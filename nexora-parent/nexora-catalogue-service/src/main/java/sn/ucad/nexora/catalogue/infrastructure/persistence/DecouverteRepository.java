package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Caracteristique;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Signal;

/**
 * Données de Nexora Découvrir (§18) : offres candidates d'une zone avec tout ce qu'il faut pour
 * les classer, profil de goûts tiré des signaux, et détails (photos, tailles…) des cartes servies.
 */
@Repository
public class DecouverteRepository {

    /** Une offre visible du public, avec ses critères de classement. */
    public record Candidat(Long idOffre, String titre, String description, BigDecimal prix, BigDecimal ancienPrix,
                           Long idCategorie, String categorie, Long idParent, Long idRacine, String rayon,
                           Long idEspace, String espaceNom, String espaceLogo, boolean verifie, boolean certifie,
                           BigDecimal note, Integer nombreAvis, String typeEspace, String telephone,
                           String commune, String quartier, Double distanceKm, LocalDateTime publication,
                           long vues, long favoris, long ventes, int nombreImages, Boolean ouvertMaintenant,
                           boolean produit, boolean service, boolean reservation, boolean domicile) {}

    /** Un espace actif de la zone ayant des offres visibles. */
    public record EspaceCandidat(Long idEspace, String nom, String logo, String couverture, String slogan, boolean verifie,
                                 boolean certifie, BigDecimal note, Integer nombreAvis, String typeEspace, String telephone,
                                 String commune, String quartier, Double distanceKm, Boolean ouvertMaintenant,
                                 long nombreOffres, Long idRacine, String rayon) {}

    /** Poids du profil par dimension : racine, catégorie parente, catégorie, espace. */
    public record ProfilBrut(Map<Long, Double> racines, Map<Long, Double> parents, Map<Long, Double> categories,
                             Map<Long, Double> espaces, Map<Long, String> noms, long nombreSignaux) {
        public static ProfilBrut vide() {
            return new ProfilBrut(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), 0);
        }
    }

    /** Poids de chaque signal (voir 19_decouverte_signal.sql). */
    private static final String POIDS = """
            CASE ds.type_signal WHEN 'PASSE' THEN -0.6 WHEN 'VUE' THEN 0.2 WHEN 'VUE_LONGUE' THEN 1
                 WHEN 'J_AIME' THEN 3 WHEN 'PARTAGE' THEN 3 WHEN 'ENREGISTRE' THEN 4 WHEN 'DETAIL' THEN 4
                 WHEN 'TAILLES' THEN 5 WHEN 'CONTACT' THEN 7 WHEN 'ACHAT' THEN 8 ELSE 0 END
            -- un signal perd la moitié de son poids en deux semaines environ
            * EXP(-EXTRACT(EPOCH FROM (NOW() - ds.date_signal)) / 86400.0 / 21)""";

    private static final String RACINES = """
            racines(id, racine) AS (
                SELECT id_categorie, id_categorie FROM categorie WHERE id_categorie_parent IS NULL
                UNION ALL
                SELECT c.id_categorie, r.racine FROM categorie c JOIN racines r ON c.id_categorie_parent = r.id
            )""";

    private static final String DISTANCE = """
            (6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(CAST(a.latitude AS DOUBLE PRECISION) - CAST(:lat AS DOUBLE PRECISION)) / 2), 2)
                + COS(RADIANS(CAST(:lat AS DOUBLE PRECISION))) * COS(RADIANS(CAST(a.latitude AS DOUBLE PRECISION)))
                  * POWER(SIN(RADIANS(CAST(a.longitude AS DOUBLE PRECISION) - CAST(:lng AS DOUBLE PRECISION)) / 2), 2))))""";

    private static final String OUVERT = """
            CASE WHEN NOT ep.ouvert THEN FALSE
                 ELSE espace_ouvert_a(ep.id_espace, CAST(NOW() AT TIME ZONE 'Africa/Dakar' AS TIMESTAMP)) END""";

    @PersistenceContext
    private EntityManager em;

    /**
     * Offres visibles d'une zone (commune ou quartier, ou rayon autour d'un point), éventuellement
     * limitées à des identifiants (flux tiré d'une recherche).
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Candidat> candidats(Collection<Long> limiterA, String zone, BigDecimal lat, BigDecimal lng, double rayonKm) {
        boolean point = lat != null && lng != null;
        StringBuilder sql = new StringBuilder("WITH RECURSIVE " + RACINES + """
                SELECT o.id_offre, o.titre, LEFT(o.description, 240), o.prix, o.ancien_prix,
                       o.id_categorie, c.nom, c.id_categorie_parent, rc.id_categorie, rc.nom,
                       ep.id_espace, ep.nom, ep.logo, ep.verifie, ep.certifie, ep.note_moyenne, ep.nombre_avis, te.nom, ep.telephone,
                       a.commune, a.quartier, %s,
                       o.date_publication, COALESCE(o.vue_count, 0),
                       (SELECT COUNT(*) FROM favori f WHERE f.id_offre = o.id_offre),
                       (SELECT COALESCE(SUM(lc.quantite), 0) FROM ligne_commande lc
                        JOIN sous_commande sc ON sc.id_sous_commande = lc.id_sous_commande
                        WHERE lc.id_offre = o.id_offre AND CAST(sc.statut AS TEXT) NOT IN ('ANNULEE', 'REMBOURSEE')),
                       (SELECT COUNT(*) FROM image i WHERE i.id_offre = o.id_offre),
                       %s,
                       EXISTS (SELECT 1 FROM produit pr WHERE pr.id_offre = o.id_offre),
                       sv.id_service IS NOT NULL, COALESCE(sv.reservation, FALSE), COALESCE(sv.intervention_domicile, FALSE)
                FROM offre o
                JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
                JOIN categorie c ON c.id_categorie = o.id_categorie
                JOIN racines r ON r.id = o.id_categorie
                JOIN categorie rc ON rc.id_categorie = r.racine
                JOIN type_espace te ON te.id_type_espace = ep.id_type_espace
                LEFT JOIN adresse a ON a.id_espace = ep.id_espace AND a.principale = TRUE
                LEFT JOIN service sv ON sv.id_offre = o.id_offre
                WHERE CAST(o.statut AS TEXT) = 'PUBLIE' AND o.disponible = TRUE
                  AND ep.ouvert = TRUE AND CAST(ep.statut AS TEXT) = 'ACTIF'
                """.formatted(point ? DISTANCE : "CAST(NULL AS DOUBLE PRECISION)", OUVERT));
        Map<String, Object> parametres = new HashMap<>();
        filtrer(sql, parametres, zone, lat, lng, rayonKm);
        if (limiterA != null) {
            if (limiterA.isEmpty()) return List.of();
            sql.append(" AND o.id_offre IN (:ids) ");
            parametres.put("ids", limiterA);
        }
        sql.append(" LIMIT 800");
        Query q = em.createNativeQuery(sql.toString());
        parametres.forEach(q::setParameter);
        List<Object[]> lignes = q.getResultList();
        List<Candidat> candidats = new ArrayList<>();
        for (Object[] l : lignes) {
            candidats.add(new Candidat(lg(l[0]), (String) l[1], (String) l[2], (BigDecimal) l[3], (BigDecimal) l[4],
                    lg(l[5]), (String) l[6], lg(l[7]), lg(l[8]), (String) l[9],
                    lg(l[10]), (String) l[11], (String) l[12], vrai(l[13]), vrai(l[14]), (BigDecimal) l[15],
                    l[16] == null ? null : ((Number) l[16]).intValue(), (String) l[17], (String) l[18],
                    (String) l[19], (String) l[20], l[21] == null ? null : ((Number) l[21]).doubleValue(),
                    date(l[22]),
                    lg0(l[23]), lg0(l[24]), lg0(l[25]), (int) lg0(l[26]), l[27] == null ? null : vrai(l[27]),
                    vrai(l[28]), vrai(l[29]), vrai(l[30]), vrai(l[31])));
        }
        return candidats;
    }

    /** Espaces actifs de la zone ayant des offres visibles, avec leur rayon principal. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<EspaceCandidat> espaces(String zone, BigDecimal lat, BigDecimal lng, double rayonKm) {
        boolean point = lat != null && lng != null;
        StringBuilder sql = new StringBuilder("WITH RECURSIVE " + RACINES + """
                , offres AS (
                    SELECT o.id_espace, r.racine, COUNT(*) AS n
                    FROM offre o JOIN racines r ON r.id = o.id_categorie
                    WHERE CAST(o.statut AS TEXT) = 'PUBLIE' AND o.disponible = TRUE
                    GROUP BY o.id_espace, r.racine
                )
                SELECT ep.id_espace, ep.nom, ep.logo, ep.couverture, ep.slogan, ep.verifie, ep.certifie, ep.note_moyenne,
                       ep.nombre_avis, te.nom, ep.telephone, a.commune, a.quartier, %s, %s,
                       (SELECT SUM(n) FROM offres x WHERE x.id_espace = ep.id_espace),
                       (SELECT x.racine FROM offres x WHERE x.id_espace = ep.id_espace ORDER BY x.n DESC LIMIT 1)
                FROM espace_professionnel ep
                JOIN type_espace te ON te.id_type_espace = ep.id_type_espace
                LEFT JOIN adresse a ON a.id_espace = ep.id_espace AND a.principale = TRUE
                WHERE ep.ouvert = TRUE AND CAST(ep.statut AS TEXT) = 'ACTIF'
                  AND EXISTS (SELECT 1 FROM offres x WHERE x.id_espace = ep.id_espace)
                """.formatted(point ? DISTANCE : "CAST(NULL AS DOUBLE PRECISION)", OUVERT));
        Map<String, Object> parametres = new HashMap<>();
        filtrer(sql, parametres, zone, lat, lng, rayonKm);
        Query q = em.createNativeQuery(sql.toString());
        parametres.forEach(q::setParameter);
        List<Object[]> lignes = q.getResultList();
        Map<Long, String> nomsRacines = nomsCategories(lignes.stream().map(l -> lg(l[16])).filter(java.util.Objects::nonNull).toList());
        List<EspaceCandidat> espaces = new ArrayList<>();
        for (Object[] l : lignes) {
            espaces.add(new EspaceCandidat(lg(l[0]), (String) l[1], (String) l[2], (String) l[3], (String) l[4], vrai(l[5]),
                    vrai(l[6]), (BigDecimal) l[7], l[8] == null ? null : ((Number) l[8]).intValue(), (String) l[9],
                    (String) l[10], (String) l[11], (String) l[12], l[13] == null ? null : ((Number) l[13]).doubleValue(),
                    l[14] == null ? null : vrai(l[14]), lg0(l[15]), lg(l[16]), nomsRacines.get(lg(l[16]))));
        }
        return espaces;
    }

    private static void filtrer(StringBuilder sql, Map<String, Object> parametres, String zone,
                                BigDecimal lat, BigDecimal lng, double rayonKm) {
        if (lat != null && lng != null) {
            sql.append(" AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL AND ").append(DISTANCE).append(" <= :rayon ");
            parametres.put("lat", lat);
            parametres.put("lng", lng);
            parametres.put("rayon", rayonKm);
        } else if (zone != null && !zone.isBlank()) {
            sql.append(" AND (a.commune ILIKE :zone OR a.quartier ILIKE :zone OR a.departement ILIKE :zone OR a.region ILIKE :zone) ");
            parametres.put("zone", zone.trim());
        }
    }

    /**
     * Profil de goûts : somme des signaux pondérés (et atténués avec le temps) des 90 derniers
     * jours, par rayon, catégorie parente, catégorie et espace.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public ProfilBrut profil(String visiteur) {
        if (visiteur == null || visiteur.isBlank()) return ProfilBrut.vide();
        List<Object[]> lignes = em.createNativeQuery("WITH RECURSIVE " + RACINES + """
                , s AS (
                    SELECT ds.id_offre, ds.id_espace, %s AS poids
                    FROM decouverte_signal ds
                    WHERE ds.visiteur = :v AND ds.date_signal > NOW() - INTERVAL '90 days'
                )
                SELECT 'RACINE', r.racine, SUM(s.poids) FROM s JOIN offre o ON o.id_offre = s.id_offre JOIN racines r ON r.id = o.id_categorie GROUP BY r.racine
                UNION ALL
                SELECT 'PARENT', c.id_categorie_parent, SUM(s.poids) FROM s JOIN offre o ON o.id_offre = s.id_offre
                    JOIN categorie c ON c.id_categorie = o.id_categorie WHERE c.id_categorie_parent IS NOT NULL GROUP BY c.id_categorie_parent
                UNION ALL
                SELECT 'CATEGORIE', o.id_categorie, SUM(s.poids) FROM s JOIN offre o ON o.id_offre = s.id_offre GROUP BY o.id_categorie
                UNION ALL
                SELECT 'ESPACE', COALESCE(s.id_espace, o.id_espace), SUM(s.poids) FROM s LEFT JOIN offre o ON o.id_offre = s.id_offre
                    GROUP BY COALESCE(s.id_espace, o.id_espace)
                UNION ALL
                SELECT 'TOTAL', NULL, COUNT(*) FROM s
                """.formatted(POIDS)).setParameter("v", visiteur).getResultList();
        Map<Long, Double> racines = new HashMap<>(), parents = new HashMap<>(), categories = new HashMap<>(), espaces = new HashMap<>();
        long total = 0;
        for (Object[] l : lignes) {
            String dimension = (String) l[0];
            if ("TOTAL".equals(dimension)) {
                total = lg0(l[2]);
                continue;
            }
            if (l[1] == null) continue;
            double poids = ((Number) l[2]).doubleValue();
            switch (dimension) {
                case "RACINE" -> racines.put(lg(l[1]), poids);
                case "PARENT" -> parents.put(lg(l[1]), poids);
                case "CATEGORIE" -> categories.put(lg(l[1]), poids);
                default -> espaces.put(lg(l[1]), poids);
            }
        }
        Set<Long> ids = new HashSet<>(racines.keySet());
        ids.addAll(parents.keySet());
        ids.addAll(categories.keySet());
        return new ProfilBrut(racines, parents, categories, espaces, nomsCategories(ids), total);
    }

    /** Offres que le visiteur a passées très vite récemment (on ne les lui remontre pas tout de suite). */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set<Long> passeesRecemment(String visiteur) {
        if (visiteur == null || visiteur.isBlank()) return Set.of();
        List<Object> ids = em.createNativeQuery("""
                SELECT DISTINCT id_offre FROM decouverte_signal
                WHERE visiteur = :v AND type_signal = 'PASSE' AND id_offre IS NOT NULL
                  AND date_signal > NOW() - INTERVAL '7 days'""").setParameter("v", visiteur).getResultList();
        Set<Long> resultat = new HashSet<>();
        ids.forEach(i -> resultat.add(lg(i)));
        return resultat;
    }

    @Transactional
    public void enregistrer(Signal s) {
        em.createNativeQuery("""
                INSERT INTO decouverte_signal (visiteur, id_offre, id_espace, type_signal, duree_ms)
                VALUES (:v, :o, :e, :t, :d)""")
                .setParameter("v", s.visiteur()).setParameter("o", s.idOffre()).setParameter("e", s.idEspace())
                .setParameter("t", s.type()).setParameter("d", s.dureeMs()).executeUpdate();
    }

    /** « Je n'aime plus » : retire les J_AIME de ce visiteur sur cette offre ou cet espace. */
    @Transactional
    public void retirerJAime(String visiteur, Long idOffre, Long idEspace) {
        em.createNativeQuery("""
                DELETE FROM decouverte_signal WHERE visiteur = :v AND type_signal = 'J_AIME'
                  AND id_offre IS NOT DISTINCT FROM :o AND id_espace IS NOT DISTINCT FROM :e""")
                .setParameter("v", visiteur).setParameter("o", idOffre).setParameter("e", idEspace).executeUpdate();
    }

    @Transactional
    public int oublier(String visiteur) {
        return em.createNativeQuery("DELETE FROM decouverte_signal WHERE visiteur = :v").setParameter("v", visiteur).executeUpdate();
    }

    /** À la connexion : l'historique anonyme du navigateur rejoint celui du compte. */
    @Transactional
    public int fusionner(String ancien, String nouveau) {
        return em.createNativeQuery("UPDATE decouverte_signal SET visiteur = :n WHERE visiteur = :a")
                .setParameter("n", nouveau).setParameter("a", ancien).executeUpdate();
    }

    /** Jusqu'à 5 médias par offre, la photo principale d'abord. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Long, List<String>> medias(Collection<Long> ids) {
        Map<Long, List<String>> medias = new HashMap<>();
        if (ids.isEmpty()) return medias;
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT id_offre, url FROM (
                    SELECT id_offre, url, ROW_NUMBER() OVER (PARTITION BY id_offre ORDER BY principale DESC NULLS LAST, ordre_affichage) AS rang
                    FROM image WHERE id_offre IN (:ids)
                ) x WHERE rang <= 5 ORDER BY id_offre, rang""").setParameter("ids", ids).getResultList();
        for (Object[] l : lignes) medias.computeIfAbsent(lg(l[0]), k -> new ArrayList<>()).add((String) l[1]);
        return medias;
    }

    /** Vitrine d'un espace : la photo principale de ses offres visibles les plus récentes (4 au plus). */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> mediasEspace(Long idEspace) {
        return em.createNativeQuery("""
                SELECT i.url FROM offre o JOIN image i ON i.id_offre = o.id_offre AND i.principale = TRUE
                WHERE o.id_espace = :e AND CAST(o.statut AS TEXT) = 'PUBLIE' AND o.disponible = TRUE
                ORDER BY o.date_publication DESC NULLS LAST LIMIT 4""").setParameter("e", idEspace).getResultList();
    }

    /** Choix multiples disponibles (tailles, couleurs, pointures…), deux caractéristiques au plus par offre. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Long, List<Caracteristique>> caracteristiques(Collection<Long> ids) {
        Map<Long, List<Caracteristique>> resultat = new HashMap<>();
        if (ids.isEmpty()) return resultat;
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT oa.id_offre, a.id_attribut, a.nom, v.valeur
                FROM offre_attribut oa
                JOIN attribut a ON a.id_attribut = oa.id_attribut
                JOIN valeur_attribut_possible v ON v.id_valeur = oa.id_valeur
                WHERE oa.id_offre IN (:ids) AND oa.epuise = FALSE AND CAST(a.type_champ AS TEXT) = 'MULTI_LISTE'
                  AND a.affichable IS NOT FALSE AND a.actif IS NOT FALSE
                ORDER BY oa.id_offre,
                         CASE WHEN a.nom ILIKE '%taille%' OR a.nom ILIKE '%pointure%' THEN 0 WHEN a.nom ILIKE '%couleur%' THEN 1 ELSE 2 END,
                         a.id_attribut, v.ordre_affichage""").setParameter("ids", ids).getResultList();
        Map<Long, Map<Long, Caracteristique>> parOffre = new LinkedHashMap<>();
        for (Object[] l : lignes) {
            Map<Long, Caracteristique> parAttribut = parOffre.computeIfAbsent(lg(l[0]), k -> new LinkedHashMap<>());
            Caracteristique c = parAttribut.computeIfAbsent(lg(l[1]), k -> new Caracteristique((String) l[2], new ArrayList<>()));
            if (!c.valeurs().contains((String) l[3])) c.valeurs().add((String) l[3]);
        }
        parOffre.forEach((offre, parAttribut) -> resultat.put(offre, parAttribut.values().stream().limit(2).toList()));
        return resultat;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, String> nomsCategories(Collection<Long> ids) {
        Map<Long, String> noms = new HashMap<>();
        if (ids.isEmpty()) return noms;
        List<Object[]> lignes = em.createNativeQuery("SELECT id_categorie, nom FROM categorie WHERE id_categorie IN (:ids)")
                .setParameter("ids", ids).getResultList();
        lignes.forEach(l -> noms.put(lg(l[0]), (String) l[1]));
        return noms;
    }

    private static LocalDateTime date(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime d) return d;
        if (o instanceof java.sql.Timestamp t) return t.toLocalDateTime();
        if (o instanceof java.time.OffsetDateTime d) return d.toLocalDateTime();
        if (o instanceof java.time.Instant i) return LocalDateTime.ofInstant(i, java.time.ZoneId.of("Africa/Dakar"));
        return LocalDateTime.parse(o.toString().replace(' ', 'T'));
    }

    private static Long lg(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static long lg0(Object o) {
        return o == null ? 0 : ((Number) o).longValue();
    }

    private static boolean vrai(Object o) {
        return Boolean.TRUE.equals(o);
    }
}
