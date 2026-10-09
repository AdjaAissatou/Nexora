package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.catalogue.domain.entity.Offre;
import sn.ucad.nexora.catalogue.domain.entity.Produit;
import sn.ucad.nexora.catalogue.domain.entity.Service;
import sn.ucad.nexora.catalogue.domain.repository.OffreRepository;
import sn.ucad.nexora.catalogue.domain.repository.RechercheParams;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.OffreJpaEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Adaptateur driven qui implémente OffreRepository.
 *
 * La méthode search() utilise une requête SQL native qui reproduit et étend
 * la vue vue_recherche_globale avec filtres dynamiques, images et
 * spécificités produit/service.
 */
@Component
public class OffreRepositoryAdapter implements OffreRepository {

    private final OffreJpaRepository jpaRepository;
    private final EntityManager em;

    public OffreRepositoryAdapter(OffreJpaRepository jpaRepository, EntityManager em) {
        this.jpaRepository = jpaRepository;
        this.em = em;
    }

    /**
     * Badge « 🔥 Populaire » (§23) : parmi les 20 % d'offres publiées au meilleur score de popularité
     * (vues, vues Découvrir, j'aime, favoris), avec au moins deux j'aime ou favoris. Le seuil est relatif :
     * il suit la vie de la plateforme. Sous-requête non corrélée : calculée une fois par requête.
     */
    public static final String POPULAIRE = "(o.nombre_jaime + o.nombre_favoris >= 2 AND o.score_popularite >= "
            + "(SELECT percentile_disc(0.8) WITHIN GROUP (ORDER BY op.score_popularite) FROM offre op "
            + "WHERE CAST(op.statut AS TEXT) = 'PUBLIE' AND op.score_popularite > 0))";

    // ===================== findById =====================

    @Override
    public Optional<Offre> findById(Long id) {
        // Requête enrichie pour la fiche détaillée
        String sql = """
            SELECT
                o.id_offre, o.id_espace, o.id_type_offre, o.id_categorie,
                o.titre, o.description, o.prix, o.ancien_prix,
                o.negociable, o.disponible, o.est_commandable, o.est_reservable,
                o.stockable, o.quantite_disponible, o.vue_count, o.score_pertinence,
                CAST(o.statut AS TEXT), o.date_creation, o.date_modification, o.date_publication,

                ep.nom AS espace_nom, ep.slogan AS espace_slogan,
                ep.logo AS espace_logo, ep.couverture AS espace_couverture,
                ep.telephone AS espace_telephone, ep.email AS espace_email,
                ep.site_web AS espace_site_web,
                ep.certifie AS espace_certifie, ep.verifie AS espace_verifie,
                ep.ouvert AS espace_ouvert,
                ep.note_moyenne AS espace_note, ep.nombre_avis,
                ep.nombre_vues AS espace_vues, ep.nombre_favoris,

                te.nom AS type_espace,
                COALESCE(o.categorie_proposee, c.nom) AS categorie_nom,
                to2.libelle AS type_offre_libelle,

                a.pays, a.region, a.departement, a.commune,
                a.arrondissement, a.quartier, a.adresse_complete, a.code_postal,
                a.latitude, a.longitude,

                p.promotion_nom, p.type_reduction, p.valeur_reduction,

                pr.marque, pr.modele, pr.reference,
                pr.quantite_stock, pr.poids, pr.garantie, pr.neuf,

                sv.duree_estimee, sv.intervention_domicile, sv.intervention_distance,
                sv.delai_reponse, sv.reservation, sv.urgence,

                o.vues_decouvrir, o.nombre_jaime, o.nombre_favoris, %POP

            FROM offre o
            JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
            JOIN categorie c ON c.id_categorie = o.id_categorie
            JOIN type_espace te ON te.id_type_espace = ep.id_type_espace
            JOIN type_offre to2 ON to2.id_type_offre = o.id_type_offre
            LEFT JOIN adresse a ON a.id_espace = ep.id_espace AND a.principale = TRUE
            LEFT JOIN (
                SELECT id_offre, id_espace,
                       nom AS promotion_nom,
                       CAST(type_reduction AS TEXT) AS type_reduction,
                       valeur AS valeur_reduction
                FROM promotion
                WHERE actif = TRUE
                  AND NOW() BETWEEN date_debut AND date_fin
            ) p ON (p.id_offre = o.id_offre OR p.id_espace = ep.id_espace)
            LEFT JOIN produit pr ON pr.id_offre = o.id_offre
            LEFT JOIN service sv ON sv.id_offre = o.id_offre
            WHERE o.id_offre = :id
            LIMIT 1
            """;

        Query q = em.createNativeQuery(sql.replace("%POP", POPULAIRE));
        q.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return Optional.empty();

        Object[] row = rows.get(0);
        Offre offre = mapRowToOffre(row);
        // Popularité (§23) : colonnes 63..66
        offre.setVuesDecouvrir(toLong(row[63]) == null ? 0 : toLong(row[63]));
        offre.setNombreJaime(toInt(row[64]) == null ? 0 : toInt(row[64]));
        offre.setNombreFavoris(toInt(row[65]) == null ? 0 : toInt(row[65]));
        offre.setPopulaire(toBool(row[66]));

        // Charger les images
        offre.setImages(loadImages(id));
        offre.setImagePrincipale(loadImagePrincipale(id));

        // Charger les tags
        offre.setTags(loadTags(id));

        return Optional.of(offre);
    }

    // ===================== search =====================

    /** Distance à vol d'oiseau (haversine, km) entre l'adresse de l'espace et le point recherché. */
    private static final String DISTANCE_KM = """
            (6371 * 2 * ASIN(SQRT(
                POWER(SIN(RADIANS(CAST(a.latitude AS DOUBLE PRECISION) - CAST(:lat AS DOUBLE PRECISION)) / 2), 2)
                + COS(RADIANS(CAST(:lat AS DOUBLE PRECISION))) * COS(RADIANS(CAST(a.latitude AS DOUBLE PRECISION)))
                  * POWER(SIN(RADIANS(CAST(a.longitude AS DOUBLE PRECISION) - CAST(:lng AS DOUBLE PRECISION)) / 2), 2))))""";
    private static final double RAYON_DEFAUT_KM = 2;

    /** Tout le texte d'une offre où chercher, sans accents ni majuscules. */
    private static final String DOCUMENT = "translate(lower(concat_ws(' ', o.titre, o.description, o.categorie_proposee, c.nom, cp.nom, cgp.nom, "
            + "to2.libelle, ep.nom, te.nom, a.commune, a.quartier, a.departement, "
            + "(SELECT string_agg(tg.nom, ' ') FROM offre_tag ot JOIN tag tg ON tg.id_tag = ot.id_tag WHERE ot.id_offre = o.id_offre))), "
            + "'" + TexteRecherche.ACCENTS + "', '" + TexteRecherche.SANS_ACCENTS + "')";
    private static final String TITRE = "translate(lower(o.titre), '" + TexteRecherche.ACCENTS + "', '"
            + TexteRecherche.SANS_ACCENTS + "')";

    /**
     * Pour chaque mot saisi, ses formes acceptées : sa racine (« plomb ») et ses synonymes entiers
     * (« canalisation », « chauffe-eau »…). Un mot sans racine utile garde sa forme entière.
     */
    private List<List<String>> termes(String q) {
        List<List<String>> termes = new java.util.ArrayList<>();
        if (q == null || q.isBlank()) return termes;
        Map<String, List<String>> synonymes = synonymes();
        for (String mot : TexteRecherche.mots(q)) {
            java.util.LinkedHashSet<String> formes = new java.util.LinkedHashSet<>();
            String racine = TexteRecherche.racine(mot);
            formes.add(racine);
            for (String equivalent : synonymes.getOrDefault(racine, List.of())) {
                formes.add(equivalent); // forme entière : la racine de « chauffeur » trouverait « chauffe-eau »
            }
            termes.add(new java.util.ArrayList<>(formes));
        }
        return termes;
    }

    /**
     * Synonymes indexés par la racine du terme (relus à chaque recherche : table courte). Un seul
     * sens : « plombier » → « canalisation », pas l'inverse, qui élargirait trop la recherche.
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> synonymes() {
        Map<String, List<String>> parRacine = new java.util.HashMap<>();
        // Table absente (base pas encore mise à jour) : recherche sans synonymes, sans erreur SQL
        Object table = em.createNativeQuery("SELECT CAST(to_regclass('synonyme_recherche') AS TEXT)").getSingleResult();
        if (table == null) return parRacine;
        List<Object[]> lignes = em.createNativeQuery("SELECT terme, equivalent FROM synonyme_recherche").getResultList();
        for (Object[] l : lignes) {
            String terme = TexteRecherche.normaliser((String) l[0]).trim();
            String equivalent = TexteRecherche.normaliser((String) l[1]).trim();
            parRacine.computeIfAbsent(TexteRecherche.racine(terme), k -> new java.util.ArrayList<>()).add(equivalent);
        }
        return parRacine;
    }

    /** Valeurs choisies regroupées par caractéristique (OU dans un groupe, ET entre groupes). */
    @SuppressWarnings("unchecked")
    private List<List<Long>> valeursParAttribut(List<Long> valeurs) {
        if (valeurs == null || valeurs.isEmpty()) return List.of();
        List<Object[]> lignes = em.createNativeQuery(
                // Groupées par nom de caractéristique : « M » de deux « Tailles disponibles » = l'une ou l'autre
                "SELECT lower(a.nom), v.id_valeur FROM valeur_attribut_possible v JOIN attribut a ON a.id_attribut = v.id_attribut"
                        + " WHERE v.id_valeur IN (:ids) ORDER BY lower(a.nom)")
                .setParameter("ids", valeurs).getResultList();
        Map<String, List<Long>> groupes = new LinkedHashMap<>();
        for (Object[] l : lignes) {
            groupes.computeIfAbsent((String) l[0], k -> new java.util.ArrayList<>()).add(((Number) l[1]).longValue());
        }
        return new java.util.ArrayList<>(groupes.values());
    }

    /** Score : 3 par mot trouvé dans le titre (forme saisie), 2 par synonyme dans le titre, 1 ailleurs. */
    private String pertinence(List<List<String>> termes, Map<String, Object> paramMap) {
        List<String> parties = new java.util.ArrayList<>();
        for (int t = 0; t < termes.size(); t++) {
            List<String> formes = termes.get(t);
            for (int v = 0; v < formes.size(); v++) {
                paramMap.put("t" + t + "_" + v, "%" + formes.get(v) + "%");
            }
            List<String> synonymes = new java.util.ArrayList<>();
            for (int v = 1; v < formes.size(); v++) synonymes.add(TITRE + " LIKE :t" + t + "_" + v);
            parties.add("(CASE WHEN " + TITRE + " LIKE :t" + t + "_0 THEN 3"
                    + (synonymes.isEmpty() ? "" : " WHEN " + String.join(" OR ", synonymes) + " THEN 2")
                    + " ELSE 1 END)");
        }
        return String.join(" + ", parties);
    }
    private static final double RAYON_MAX_KM = 50;

    @Override
    @SuppressWarnings("unchecked")
    public List<Offre> search(RechercheParams params) {
        return (List<Offre>) executer(params, false);
    }

    /** Nombre total d'offres correspondant à la recherche (toutes pages confondues). */
    @Override
    public long count(RechercheParams params) {
        return (Long) executer(params, true);
    }

    /** Construit la recherche ; {@code compter} : renvoie le nombre de résultats au lieu de la page. */
    private Object executer(RechercheParams params, boolean compter) {
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT ON (o.id_offre)
                o.id_offre, o.id_espace, o.id_type_offre, o.id_categorie,
                o.titre, o.description, o.prix, o.ancien_prix,
                o.negociable, o.disponible, o.est_commandable, o.est_reservable,
                o.stockable, o.quantite_disponible, o.vue_count, o.score_pertinence,
                CAST(o.statut AS TEXT), o.date_creation, o.date_modification, o.date_publication,

                ep.nom AS espace_nom, ep.slogan AS espace_slogan,
                ep.logo AS espace_logo, ep.couverture AS espace_couverture,
                ep.telephone AS espace_telephone, ep.email AS espace_email,
                ep.site_web AS espace_site_web,
                ep.certifie AS espace_certifie, ep.verifie AS espace_verifie,
                ep.ouvert AS espace_ouvert,
                ep.note_moyenne AS espace_note, ep.nombre_avis,
                ep.nombre_vues AS espace_vues, ep.nombre_favoris,

                te.nom AS type_espace,
                COALESCE(o.categorie_proposee, c.nom) AS categorie_nom,
                to2.libelle AS type_offre_libelle,

                a.pays, a.region, a.departement, a.commune,
                a.arrondissement, a.quartier, a.adresse_complete, a.code_postal,
                a.latitude, a.longitude,

                p.promotion_nom, p.type_reduction, p.valeur_reduction,

                NULL::VARCHAR AS marque, NULL::VARCHAR AS modele,
                NULL::VARCHAR AS reference,
                NULL::INTEGER AS quantite_stock, NULL::FLOAT AS poids,
                NULL::VARCHAR AS garantie, NULL::BOOLEAN AS neuf,

                NULL::INTEGER AS duree_estimee,
                NULL::BOOLEAN AS intervention_domicile,
                NULL::BOOLEAN AS intervention_distance,
                NULL::INTEGER AS delai_reponse,
                NULL::BOOLEAN AS reservation,
                NULL::BOOLEAN AS urgence,

                img.url AS image_principale,
                o.motif_moderation,
                -- État d'ouverture de l'espace à l'heure de Dakar (NULL sans horaires, §10) ; faux s'il est fermé par le pro
                CASE WHEN NOT ep.ouvert THEN FALSE
                     ELSE espace_ouvert_a(ep.id_espace, CAST(NOW() AT TIME ZONE 'Africa/Dakar' AS TIMESTAMP)) END AS espace_ouvert_maintenant,
                -- Distance au point de recherche, en km (NULL hors recherche autour d'un point)
                %s AS distance_km,
                -- Pertinence du texte saisi : mots trouvés dans le titre d'abord (0 sans texte)
                %p AS pertinence_texte,
                -- Popularité (§17) : quantité vendue hors commandes annulées, puis favoris
                (SELECT COALESCE(SUM(lc.quantite), 0) FROM ligne_commande lc
                 JOIN sous_commande sc ON sc.id_sous_commande = lc.id_sous_commande
                 WHERE lc.id_offre = o.id_offre AND CAST(sc.statut AS TEXT) NOT IN ('ANNULEE', 'REMBOURSEE')) AS ventes,
                o.nombre_favoris AS favoris,
                -- Popularité (§23) : colonnes 70..74
                o.vues_decouvrir, o.nombre_jaime, o.nombre_favoris AS nombre_favoris_offre, %POP AS populaire,
                o.score_popularite

            FROM offre o
            JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
            JOIN categorie c ON c.id_categorie = o.id_categorie
            JOIN type_espace te ON te.id_type_espace = ep.id_type_espace
            JOIN type_offre to2 ON to2.id_type_offre = o.id_type_offre
            LEFT JOIN categorie cp ON cp.id_categorie = c.id_categorie_parent
            LEFT JOIN categorie cgp ON cgp.id_categorie = cp.id_categorie_parent
            LEFT JOIN adresse a ON a.id_espace = ep.id_espace AND a.principale = TRUE
            LEFT JOIN (
                SELECT id_offre, id_espace,
                       nom AS promotion_nom,
                       CAST(type_reduction AS TEXT) AS type_reduction,
                       valeur AS valeur_reduction
                FROM promotion
                WHERE actif = TRUE AND NOW() BETWEEN date_debut AND date_fin
            ) p ON (p.id_offre = o.id_offre OR p.id_espace = ep.id_espace)
            LEFT JOIN image img ON img.id_offre = o.id_offre AND img.principale = TRUE
            """);
        marqueur(sql, "%POP", POPULAIRE);
        String distance = params.isAutourDunPoint() ? DISTANCE_KM : "CAST(NULL AS DOUBLE PRECISION)";
        int marque = sql.indexOf("%s AS distance_km");
        sql.replace(marque, marque + 2, distance);
        Map<String, Object> paramMap = new LinkedHashMap<>();
        List<List<String>> termes = termes(params.getQ());
        String pertinence = termes.isEmpty() ? "0" : pertinence(termes, paramMap);
        marque = sql.indexOf("%p AS pertinence_texte");
        sql.replace(marque, marque + 2, pertinence);

        // Recherche publique : seulement ce qui est visible. Gestion : tout ce que le professionnel possède.
        sql.append(params.isGestion()
                ? " WHERE CAST(o.statut AS TEXT) <> 'SUPPRIME' "
                : " WHERE CAST(o.statut AS TEXT) = 'PUBLIE' AND ep.ouvert = TRUE AND CAST(ep.statut AS TEXT) = 'ACTIF' ");

        if (params.getIdsOffres() != null) {
            if (params.getIdsOffres().isEmpty()) return List.of();
            sql.append(" AND o.id_offre IN (:idsOffres) ");
            paramMap.put("idsOffres", params.getIdsOffres());
        }

        // Texte libre : chaque mot (ou sa racine, ou un synonyme) doit figurer quelque part dans
        // l'offre, son espace, ses catégories ou ses tags, accents ignorés (§16).
        for (int t = 0; t < termes.size(); t++) {
            List<String> conditions = new java.util.ArrayList<>();
            for (int v = 0; v < termes.get(t).size(); v++) {
                conditions.add(DOCUMENT + " LIKE :t" + t + "_" + v);
                // « adjashop » trouve « Adja Shop » : même texte, espaces retirés
                if (v == 0) conditions.add("replace(" + DOCUMENT + ", ' ', '') LIKE :t" + t + "_0");
            }
            sql.append(" AND (").append(String.join(" OR ", conditions)).append(") ");
        }

        if (params.getCategorie() != null && !params.getCategorie().isBlank()) {
            sql.append(" AND c.nom ILIKE :cat ");
            paramMap.put("cat", "%" + params.getCategorie().trim() + "%");
        }

        if (params.getIdCategorie() != null) {
            sql.append("""
                AND o.id_categorie IN (
                    WITH RECURSIVE descendants AS (
                        SELECT id_categorie FROM categorie WHERE id_categorie = :idCategorie
                        UNION ALL
                        SELECT c2.id_categorie FROM categorie c2
                        JOIN descendants d ON c2.id_categorie_parent = d.id_categorie
                    )
                    SELECT id_categorie FROM descendants
                )
                """);
            paramMap.put("idCategorie", params.getIdCategorie());
        }

        if (params.getTypeEspace() != null && !params.getTypeEspace().isBlank()) {
            sql.append(" AND te.nom ILIKE :te ");
            paramMap.put("te", "%" + params.getTypeEspace().trim() + "%");
        }

        if (params.getCommune() != null && !params.getCommune().isBlank()) {
            sql.append(" AND a.commune ILIKE :commune ");
            paramMap.put("commune", "%" + params.getCommune().trim() + "%");
        }

        if (params.getRegion() != null && !params.getRegion().isBlank()) {
            sql.append(" AND a.region ILIKE :region ");
            paramMap.put("region", "%" + params.getRegion().trim() + "%");
        }

        if (params.getIdEspace() != null) {
            sql.append(" AND o.id_espace = :idEspace ");
            paramMap.put("idEspace", params.getIdEspace());
        }

        if (params.getPrixMin() != null) {
            sql.append(" AND o.prix >= :prixMin ");
            paramMap.put("prixMin", params.getPrixMin());
        }

        if (params.getPrixMax() != null) {
            sql.append(" AND o.prix <= :prixMax ");
            paramMap.put("prixMax", params.getPrixMax());
        }

        if (Boolean.TRUE.equals(params.getDisponible())) {
            sql.append(" AND o.disponible = TRUE ");
        }

        if (Boolean.TRUE.equals(params.getAvecPromotion())) {
            // Promotion en cours, ou prix barré (ancien prix supérieur)
            sql.append(" AND (p.promotion_nom IS NOT NULL OR o.ancien_prix > o.prix) ");
        }

        if (params.getNoteMin() != null) {
            sql.append(" AND ep.note_moyenne >= :noteMin ");
            paramMap.put("noteMin", params.getNoteMin());
        }

        if (params.getNeuf() != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM produit pn WHERE pn.id_offre = o.id_offre AND pn.neuf IS NOT DISTINCT FROM :neuf) ");
            paramMap.put("neuf", params.getNeuf());
        }

        if (Boolean.TRUE.equals(params.getNegociable())) {
            sql.append(" AND o.negociable = TRUE ");
        }

        if (Boolean.TRUE.equals(params.getPopulaire())) {
            sql.append(" AND ").append(POPULAIRE).append(" ");
        }

        if (Boolean.TRUE.equals(params.getDomicile())) {
            sql.append(" AND EXISTS (SELECT 1 FROM service sd WHERE sd.id_offre = o.id_offre AND sd.intervention_domicile = TRUE) ");
        }

        // Caractéristiques : une valeur au moins par caractéristique choisie (M ou L, et Noir), non épuisée
        int groupe = 0;
        for (List<Long> ids : valeursParAttribut(params.getValeurs())) {
            sql.append(" AND EXISTS (SELECT 1 FROM offre_attribut oav WHERE oav.id_offre = o.id_offre AND oav.epuise = FALSE"
                    + " AND oav.id_valeur IN (:valeurs").append(groupe).append(")) ");
            paramMap.put("valeurs" + groupe, ids);
            groupe++;
        }

        if (Boolean.TRUE.equals(params.getOuvertMaintenant())) {
            sql.append(" AND ep.ouvert AND espace_ouvert_a(ep.id_espace, CAST(NOW() AT TIME ZONE 'Africa/Dakar' AS TIMESTAMP)) IS TRUE ");
        }

        if (Boolean.TRUE.equals(params.getEspaceVerifie())) {
            sql.append(" AND ep.verifie = TRUE ");
        }

        if (params.getEstProduit() != null) {
            if (params.getEstProduit()) {
                sql.append(" AND EXISTS (SELECT 1 FROM produit pr WHERE pr.id_offre = o.id_offre) ");
            } else {
                sql.append(" AND EXISTS (SELECT 1 FROM service sv WHERE sv.id_offre = o.id_offre) ");
            }
        }

        if (params.isAutourDunPoint()) {
            sql.append(" AND a.latitude IS NOT NULL AND a.longitude IS NOT NULL AND ").append(DISTANCE_KM).append(" <= :rayonKm ");
            paramMap.put("lat", params.getLat());
            paramMap.put("lng", params.getLng());
            double rayon = params.getRayonKm() == null ? RAYON_DEFAUT_KM : params.getRayonKm();
            paramMap.put("rayonKm", Math.max(0.1, Math.min(rayon, RAYON_MAX_KM)));
        }

        // Tie-breaker fixe pour DISTINCT ON (id_offre obligatoire en tête d'ORDER BY ici) :
        // le tri réellement demandé par l'utilisateur est appliqué dans la requête englobante,
        // car un ORDER BY secondaire à cet endroit serait ignoré (DISTINCT ON impose id_offre en clé primaire de tri).
        sql.append(" ORDER BY o.id_offre ");

        if (compter) {
            Query c = em.createNativeQuery("SELECT COUNT(*) FROM (" + sql + ") base");
            paramMap.forEach(c::setParameter);
            return ((Number) c.getSingleResult()).longValue();
        }

        String tri = params.isAutourDunPoint() && (params.getTri() == null || "PERTINENCE".equalsIgnoreCase(params.getTri()))
                ? "DISTANCE" : params.getTri();
        String requeteFinale = "SELECT * FROM (" + sql + ") base " + buildOrderBy(tri)
                + " LIMIT :taille OFFSET :offset ";

        paramMap.put("taille", params.getTaille());
        paramMap.put("offset", params.getPage() * params.getTaille());

        Query q = em.createNativeQuery(requeteFinale);
        paramMap.forEach(q::setParameter);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        return rows.stream()
                .map(this::mapSearchRowToOffre)
                .toList();
    }

    // ===================== incrementerVues =====================

    @Override
    public void incrementerVues(Long idOffre) {
        jpaRepository.incrementerVues(idOffre);
    }

    // ===================== HELPERS =====================

    private String buildOrderBy(String tri) {
        return switch (tri == null ? "PERTINENCE" : tri.toUpperCase()) {
            case "PRIX_ASC"   -> " ORDER BY prix ASC NULLS LAST ";
            case "PRIX_DESC"  -> " ORDER BY prix DESC NULLS LAST ";
            case "DATE_DESC"  -> " ORDER BY date_publication DESC NULLS LAST ";
            case "NOTE"       -> " ORDER BY espace_note DESC NULLS LAST ";
            case "POPULARITE" -> " ORDER BY (score_popularite + 10 * ventes) DESC, espace_note DESC NULLS LAST, id_offre ";
            case "REMISE"     -> " ORDER BY CASE WHEN ancien_prix > prix THEN (ancien_prix - prix) / ancien_prix ELSE 0 END DESC, prix ASC ";
            case "DISTANCE"   -> " ORDER BY distance_km ASC NULLS LAST, pertinence_texte DESC, score_pertinence DESC ";
            default           -> " ORDER BY pertinence_texte DESC, score_pertinence DESC, vue_count DESC ";
        };
    }

    /** Mapper pour la fiche détaillée (findById) — colonnes 0..51 */
    private Offre mapRowToOffre(Object[] r) {
        Offre o = new Offre();
        o.setId(toLong(r[0]));
        o.setEspaceId(toLong(r[1]));
        o.setTypeOffreId(toLong(r[2]));
        o.setCategorieId(toLong(r[3]));
        o.setTitre(str(r[4]));
        o.setDescription(str(r[5]));
        o.setPrix(toBD(r[6]));
        o.setAncienPrix(toBD(r[7]));
        o.setNegociable(toBool(r[8]));
        o.setDisponible(toBool(r[9]));
        o.setEstCommandable(toBool(r[10]));
        o.setEstReservable(toBool(r[11]));
        o.setStockable(toBool(r[12]));
        o.setQuantiteDisponible(toInt(r[13]));
        o.setVueCount(toLong(r[14]));
        o.setScorePertinence(toDouble(r[15]));
        o.setStatut(str(r[16]));
        o.setDateCreation(toDateTime(r[17]));
        o.setDateModification(toDateTime(r[18]));
        o.setDatePublication(toDateTime(r[19]));

        o.setEspaceNom(str(r[20]));
        // r[21] = slogan (non mappé sur Offre domaine simple)
        o.setEspaceLogo(str(r[22]));
        // r[23] = couverture
        o.setEspaceTelephone(str(r[24]));
        // r[25] = email, r[26] = site_web
        o.setEspaceCertifie(toBool(r[27]));
        o.setEspaceVerifie(toBool(r[28]));
        o.setEspaceOuvert(toBool(r[29]));
        o.setEspaceNoteMoyenne(toBD(r[30]));
        o.setEspaceNombreAvis(toInt(r[31]));

        o.setTypeEspace(str(r[34]));
        o.setCategorie(str(r[35]));

        o.setPays(str(r[37]));
        o.setRegion(str(r[38]));
        o.setDepartement(str(r[39]));
        o.setCommune(str(r[40]));
        // r[41] = arrondissement, r[42] = quartier, r[43] = adresse_complete, r[44] = code_postal
        o.setQuartier(str(r[42]));
        o.setAdresseComplete(str(r[43]));
        o.setLatitude(toBD(r[45]));
        o.setLongitude(toBD(r[46]));

        o.setPromotionNom(str(r[47]));
        o.setTypeReduction(str(r[48]));
        o.setValeurReduction(toBD(r[49]));

        // Produit (r[50]..r[56])
        if (r[50] != null || r[51] != null) {
            Produit p = new Produit();
            p.setMarque(str(r[50]));
            p.setModele(str(r[51]));
            p.setReference(str(r[52]));
            p.setQuantiteStock(toInt(r[53]));
            p.setPoids(toDouble(r[54]));
            p.setGarantie(str(r[55]));
            p.setNeuf(toBool(r[56]));
            o.setProduit(p);
        }

        // Service (r[57]..r[62])
        if (r[57] != null) {
            Service s = new Service();
            s.setDureeEstimee(toInt(r[57]));
            s.setInterventionDomicile(toBool(r[58]));
            s.setInterventionDistance(toBool(r[59]));
            s.setDelaiReponse(toInt(r[60]));
            s.setReservation(toBool(r[61]));
            s.setUrgence(toBool(r[62]));
            o.setService(s);
        }

        return o;
    }

    /** Mapper allégé pour la liste de recherche (dernière colonne = image_principale) */
    private Offre mapSearchRowToOffre(Object[] r) {
        Offre o = mapRowToOffre(r);
        // Dernière colonne : image_principale (index 63)
        if (r.length > 63) {
            o.setImagePrincipale(str(r[63]));
        }
        // Puis motif_moderation (index 64)
        if (r.length > 64) {
            o.setMotifModeration(str(r[64]));
        }
        // Puis espace_ouvert_maintenant (index 65)
        if (r.length > 65) {
            o.setEspaceOuvertMaintenant(r[65] == null ? null : toBool(r[65]));
        }
        // Puis distance_km (index 66), pertinence_texte (67), ventes (68), favoris (69)
        if (r.length > 66 && r[66] != null) {
            o.setDistanceKm(Math.round(((Number) r[66]).doubleValue() * 100) / 100.0);
        }
        if (r.length > 68 && r[68] != null) {
            o.setNombreVentes(((Number) r[68]).longValue());
        }
        // Popularité (§23) : vues Découvrir (70), j'aime (71), favoris (72), populaire (73)
        if (r.length > 73) {
            o.setVuesDecouvrir(r[70] == null ? 0 : ((Number) r[70]).longValue());
            o.setNombreJaime(r[71] == null ? 0 : ((Number) r[71]).intValue());
            o.setNombreFavoris(r[72] == null ? 0 : ((Number) r[72]).intValue());
            o.setPopulaire(toBool(r[73]));
        }
        return o;
    }

    /** Remplace la première occurrence de {@code marque} dans la requête. */
    private static void marqueur(StringBuilder sql, String marque, String valeur) {
        int i = sql.indexOf(marque);
        if (i >= 0) sql.replace(i, i + marque.length(), valeur);
    }

    private List<String> loadImages(Long offreId) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
                "SELECT url FROM image WHERE id_offre = :id ORDER BY ordre_affichage ASC")
                .setParameter("id", offreId)
                .getResultList();
        return rows.stream().map(this::str).filter(Objects::nonNull).toList();
    }

    private String loadImagePrincipale(Long offreId) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
                "SELECT url FROM image WHERE id_offre = :id AND principale = TRUE LIMIT 1")
                .setParameter("id", offreId)
                .getResultList();
        return rows.isEmpty() ? null : str(rows.get(0));
    }

    private List<String> loadTags(Long offreId) {
        @SuppressWarnings("unchecked")
        List<Object> rows = em.createNativeQuery(
                "SELECT t.nom FROM tag t JOIN offre_tag ot ON ot.id_tag = t.id_tag WHERE ot.id_offre = :id")
                .setParameter("id", offreId)
                .getResultList();
        return rows.stream().map(r -> str(r)).filter(Objects::nonNull).toList();
    }

    // ===== Utilitaires de conversion =====

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(v.toString());
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    private BigDecimal toBD(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }

    private boolean toBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    private String str(Object v) {
        return v == null ? null : v.toString();
    }

    private LocalDateTime toDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}
