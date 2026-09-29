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
                c.nom AS categorie_nom,
                to2.libelle AS type_offre_libelle,

                a.pays, a.region, a.departement, a.commune,
                a.arrondissement, a.quartier, a.adresse_complete, a.code_postal,
                a.latitude, a.longitude,

                p.promotion_nom, p.type_reduction, p.valeur_reduction,

                pr.marque, pr.modele, pr.reference,
                pr.quantite_stock, pr.poids, pr.garantie, pr.neuf,

                sv.duree_estimee, sv.intervention_domicile, sv.intervention_distance,
                sv.delai_reponse, sv.reservation, sv.urgence

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

        Query q = em.createNativeQuery(sql);
        q.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return Optional.empty();

        Object[] row = rows.get(0);
        Offre offre = mapRowToOffre(row);

        // Charger les images
        offre.setImages(loadImages(id));
        offre.setImagePrincipale(loadImagePrincipale(id));

        // Charger les tags
        offre.setTags(loadTags(id));

        return Optional.of(offre);
    }

    // ===================== search =====================

    @Override
    public List<Offre> search(RechercheParams params) {
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
                c.nom AS categorie_nom,
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

                img.url AS image_principale

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
                WHERE actif = TRUE AND NOW() BETWEEN date_debut AND date_fin
            ) p ON (p.id_offre = o.id_offre OR p.id_espace = ep.id_espace)
            LEFT JOIN image img ON img.id_offre = o.id_offre AND img.principale = TRUE
            WHERE CAST(o.statut AS TEXT) = 'PUBLIE'
              AND ep.ouvert = TRUE
              AND CAST(ep.statut AS TEXT) = 'ACTIF'
            """);

        Map<String, Object> paramMap = new LinkedHashMap<>();

        // Filtre texte libre
        if (params.getQ() != null && !params.getQ().isBlank()) {
            sql.append("""
                AND (
                    o.titre ILIKE :q
                    OR o.description ILIKE :q
                    OR c.nom ILIKE :q
                    OR a.commune ILIKE :q
                    OR a.quartier ILIKE :q
                )
                """);
            paramMap.put("q", "%" + params.getQ().trim() + "%");
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
            sql.append(" AND p.promotion_nom IS NOT NULL ");
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

        // Tri
        sql.append(buildOrderBy(params.getTri()));

        // Pagination
        sql.append(" LIMIT :taille OFFSET :offset ");
        paramMap.put("taille", params.getTaille());
        paramMap.put("offset", params.getPage() * params.getTaille());

        Query q = em.createNativeQuery(sql.toString());
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
            case "PRIX_ASC"   -> " ORDER BY o.id_offre, o.prix ASC NULLS LAST ";
            case "PRIX_DESC"  -> " ORDER BY o.id_offre, o.prix DESC NULLS LAST ";
            case "DATE_DESC"  -> " ORDER BY o.id_offre, o.date_publication DESC NULLS LAST ";
            case "NOTE"       -> " ORDER BY o.id_offre, ep.note_moyenne DESC NULLS LAST ";
            default           -> " ORDER BY o.id_offre, o.score_pertinence DESC, o.vue_count DESC ";
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
        return o;
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
