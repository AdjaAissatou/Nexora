package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse.CategorieExploree;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse.EspaceSurCarte;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse.LieuExplore;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse.Quartier;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse.SousCategorie;

/**
 * Comptages de la page « Explorer » (requêtes natives, comme la recherche). Une offre compte
 * si le public peut la voir : publiée, disponible, dans un espace actif et ouvert.
 */
@Repository
public class ExplorerQueryRepository {

    /** Rayon autour d'un lieu public pour compter les espaces « à côté ». */
    public static final double RAYON_LIEU_KM = 1.5;

    private static final String OFFRES_VISIBLES = """
            SELECT o.id_offre, o.id_categorie, o.id_espace
            FROM offre o JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
            WHERE CAST(o.statut AS TEXT) = 'PUBLIE' AND o.disponible = TRUE
              AND ep.ouvert = TRUE AND CAST(ep.statut AS TEXT) = 'ACTIF'
            """;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public ExplorerResponse explorer() {
        return new ExplorerResponse(categories(), quartiers(), lieux(), espaces());
    }

    /** Catégories racines ayant des offres, avec leurs sous-catégories directes les plus fournies. */
    @SuppressWarnings("unchecked")
    private List<CategorieExploree> categories() {
        List<Object[]> lignes = em.createNativeQuery("""
                WITH RECURSIVE arbre(id, racine, sous) AS (
                    SELECT id_categorie, id_categorie, CAST(NULL AS BIGINT) FROM categorie
                    WHERE id_categorie_parent IS NULL AND actif IS NOT FALSE
                    UNION ALL
                    SELECT c.id_categorie, a.racine, COALESCE(a.sous, c.id_categorie)
                    FROM categorie c JOIN arbre a ON c.id_categorie_parent = a.id
                    WHERE c.actif IS NOT FALSE
                ),
                visibles AS (%s)
                SELECT r.id_categorie, r.nom, r.icone, r.couleur, s.id_categorie, s.nom, COUNT(*)
                FROM visibles v
                JOIN arbre a ON a.id = v.id_categorie
                JOIN categorie r ON r.id_categorie = a.racine
                LEFT JOIN categorie s ON s.id_categorie = a.sous
                GROUP BY r.id_categorie, r.nom, r.icone, r.couleur, r.ordre_affichage, s.id_categorie, s.nom
                ORDER BY r.ordre_affichage NULLS LAST, r.nom, COUNT(*) DESC, s.nom
                """.formatted(OFFRES_VISIBLES)).getResultList();
        Map<Long, CategorieExploree> parRacine = new LinkedHashMap<>();
        Map<Long, long[]> totaux = new LinkedHashMap<>();
        for (Object[] l : lignes) {
            Long id = ((Number) l[0]).longValue();
            long nombre = ((Number) l[6]).longValue();
            parRacine.computeIfAbsent(id, k -> new CategorieExploree(k, (String) l[1], (String) l[2], (String) l[3], 0, new ArrayList<>()));
            totaux.computeIfAbsent(id, k -> new long[1])[0] += nombre;
            if (l[4] != null) {
                parRacine.get(id).sousCategories().add(new SousCategorie(((Number) l[4]).longValue(), (String) l[5], nombre));
            }
        }
        List<CategorieExploree> resultat = new ArrayList<>();
        for (CategorieExploree c : parRacine.values()) {
            resultat.add(new CategorieExploree(c.id(), c.nom(), c.icone(), c.couleur(), totaux.get(c.id())[0], c.sousCategories()));
        }
        resultat.sort((a, b) -> Long.compare(b.nombreOffres(), a.nombreOffres()));
        return resultat;
    }

    @SuppressWarnings("unchecked")
    private List<Quartier> quartiers() {
        List<Object[]> lignes = em.createNativeQuery("""
                WITH visibles AS (%s)
                SELECT a.commune, MAX(a.region), COUNT(DISTINCT v.id_espace), COUNT(*)
                FROM visibles v JOIN adresse a ON a.id_espace = v.id_espace AND a.principale = TRUE
                WHERE a.commune IS NOT NULL AND a.commune <> ''
                GROUP BY a.commune
                ORDER BY COUNT(DISTINCT v.id_espace) DESC, COUNT(*) DESC, a.commune
                """.formatted(OFFRES_VISIBLES)).getResultList();
        return lignes.stream().map(l -> new Quartier((String) l[0], (String) l[1],
                ((Number) l[2]).longValue(), ((Number) l[3]).longValue())).toList();
    }

    /** Lieux publics ayant au moins un espace (avec des offres visibles) à proximité. */
    @SuppressWarnings("unchecked")
    private List<LieuExplore> lieux() {
        List<Object[]> lignes = em.createNativeQuery("""
                WITH visibles AS (%s),
                espaces AS (
                    SELECT DISTINCT a.id_espace, CAST(a.latitude AS DOUBLE PRECISION) AS lat, CAST(a.longitude AS DOUBLE PRECISION) AS lng
                    FROM visibles v JOIN adresse a ON a.id_espace = v.id_espace AND a.principale = TRUE
                    WHERE a.latitude IS NOT NULL AND a.longitude IS NOT NULL
                )
                SELECT l.id_lieu, l.nom, l.type_lieu, l.commune, l.latitude, l.longitude, COUNT(e.id_espace)
                FROM lieu_public l
                JOIN espaces e ON 6371 * 2 * ASIN(SQRT(
                        POWER(SIN(RADIANS(e.lat - CAST(l.latitude AS DOUBLE PRECISION)) / 2), 2)
                        + COS(RADIANS(CAST(l.latitude AS DOUBLE PRECISION))) * COS(RADIANS(e.lat))
                          * POWER(SIN(RADIANS(e.lng - CAST(l.longitude AS DOUBLE PRECISION)) / 2), 2))) <= :rayon
                WHERE l.actif = TRUE
                GROUP BY l.id_lieu, l.nom, l.type_lieu, l.commune, l.latitude, l.longitude
                ORDER BY (l.type_lieu = 'MARCHE') DESC, COUNT(e.id_espace) DESC, l.nom
                LIMIT 12
                """.formatted(OFFRES_VISIBLES)).setParameter("rayon", RAYON_LIEU_KM).getResultList();
        return lignes.stream().map(l -> new LieuExplore(((Number) l[0]).longValue(), (String) l[1], (String) l[2],
                (String) l[3], (BigDecimal) l[4], (BigDecimal) l[5], ((Number) l[6]).longValue(), RAYON_LIEU_KM)).toList();
    }

    @SuppressWarnings("unchecked")
    private List<EspaceSurCarte> espaces() {
        List<Object[]> lignes = em.createNativeQuery("""
                WITH visibles AS (%s)
                SELECT ep.id_espace, ep.nom, te.nom, a.commune, a.latitude, a.longitude, COUNT(*)
                FROM visibles v
                JOIN espace_professionnel ep ON ep.id_espace = v.id_espace
                JOIN type_espace te ON te.id_type_espace = ep.id_type_espace
                JOIN adresse a ON a.id_espace = ep.id_espace AND a.principale = TRUE
                WHERE a.latitude IS NOT NULL AND a.longitude IS NOT NULL
                GROUP BY ep.id_espace, ep.nom, te.nom, a.commune, a.latitude, a.longitude
                ORDER BY ep.nom
                """.formatted(OFFRES_VISIBLES)).getResultList();
        return lignes.stream().map(l -> new EspaceSurCarte(((Number) l[0]).longValue(), (String) l[1], (String) l[2],
                (String) l[3], (BigDecimal) l[4], (BigDecimal) l[5], ((Number) l[6]).longValue())).toList();
    }

    /**
     * Espaces actifs dont le nom (ou le slogan) contient le texte, sans tenir compte des accents,
     * des majuscules ni des espaces : « adjashop » trouve « Adja Shop ».
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<sn.ucad.nexora.catalogue.application.dto.response.EspaceTrouveResponse> espacesParNom(String texte, int limite) {
        String q = TexteRecherche.normaliser(texte).replaceAll("[^a-z0-9]", "");
        if (q.length() < 2) return List.of();
        String nom = "replace(replace(translate(lower(%s), :accents, :sans), ' ', ''), '-', '')";
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT ep.id_espace, ep.nom, ep.slogan, ep.logo, te.nom, a.commune, a.quartier, ep.verifie, ep.certifie,
                       ep.note_moyenne, ep.nombre_avis,
                       (SELECT COUNT(*) FROM offre o WHERE o.id_espace = ep.id_espace
                          AND CAST(o.statut AS TEXT) = 'PUBLIE' AND o.disponible = TRUE),
                       CASE WHEN NOT ep.ouvert THEN FALSE
                            ELSE espace_ouvert_a(ep.id_espace, CAST(NOW() AT TIME ZONE 'Africa/Dakar' AS TIMESTAMP)) END
                FROM espace_professionnel ep
                JOIN type_espace te ON te.id_type_espace = ep.id_type_espace
                LEFT JOIN adresse a ON a.id_espace = ep.id_espace AND a.principale = TRUE
                WHERE CAST(ep.statut AS TEXT) = 'ACTIF'
                  AND (%s LIKE :q OR %s LIKE :q)
                ORDER BY (%s LIKE :debut) DESC, ep.verifie DESC, ep.note_moyenne DESC NULLS LAST, ep.nom
                LIMIT :limite""".formatted(nom.formatted("ep.nom"), nom.formatted("COALESCE(ep.slogan, '')"), nom.formatted("ep.nom")))
                .setParameter("accents", TexteRecherche.ACCENTS).setParameter("sans", TexteRecherche.SANS_ACCENTS)
                .setParameter("q", "%" + q + "%").setParameter("debut", q + "%").setParameter("limite", limite)
                .getResultList();
        return lignes.stream().map(l -> new sn.ucad.nexora.catalogue.application.dto.response.EspaceTrouveResponse(
                ((Number) l[0]).longValue(), (String) l[1], (String) l[2], (String) l[3], (String) l[4], (String) l[5],
                (String) l[6], Boolean.TRUE.equals(l[7]), Boolean.TRUE.equals(l[8]), (BigDecimal) l[9],
                l[10] == null ? null : ((Number) l[10]).intValue(), ((Number) l[11]).longValue(),
                l[12] == null ? null : Boolean.TRUE.equals(l[12]))).toList();
    }
}
