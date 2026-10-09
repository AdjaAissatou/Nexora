package sn.ucad.nexora.web.client;

import java.math.BigDecimal;

/**
 * Critères de recherche d'offres, tels qu'attendus par
 * {@code GET /api/v1/offres/recherche} de catalogue-service. Tous les champs sont optionnels.
 */
public record CritereRecherche(
        String q,
        String categorie,
        Long idCategorie,
        String typeEspace,
        String commune,
        String region,
        Long idEspace,
        BigDecimal prixMin,
        BigDecimal prixMax,
        Boolean estProduit,
        Boolean avecPromotion,
        Boolean espaceVerifie,
        /** Uniquement les espaces ouverts en ce moment (horaires, heure de Dakar). */
        Boolean ouvertMaintenant,
        String tri,
        int page,
        int taille,
        /** Autour d'un point (lieu public) : latitude, longitude et rayon en km. */
        BigDecimal lat,
        BigDecimal lng,
        Double rayonKm,
        /** Filtres avancés (§17) : note minimale, neuf/occasion, négociable, à domicile, caractéristiques. */
        BigDecimal noteMin,
        Boolean neuf,
        Boolean negociable,
        Boolean domicile,
        java.util.List<Long> valeurs,
        /** Uniquement les offres « 🔥 Populaire » (§23). */
        Boolean populaire) {

    public CritereRecherche(String q, String categorie, Long idCategorie, String typeEspace, String commune,
                            String region, Long idEspace, BigDecimal prixMin, BigDecimal prixMax,
                            Boolean estProduit, Boolean avecPromotion, Boolean espaceVerifie,
                            Boolean ouvertMaintenant, String tri, int page, int taille,
                            BigDecimal lat, BigDecimal lng, Double rayonKm) {
        this(q, categorie, idCategorie, typeEspace, commune, region, idEspace, prixMin, prixMax, estProduit,
                avecPromotion, espaceVerifie, ouvertMaintenant, tri, page, taille, lat, lng, rayonKm,
                null, null, null, null, null, null);
    }

    public CritereRecherche(String q, String categorie, Long idCategorie, String typeEspace, String commune,
                            String region, Long idEspace, BigDecimal prixMin, BigDecimal prixMax,
                            Boolean estProduit, Boolean avecPromotion, Boolean espaceVerifie,
                            Boolean ouvertMaintenant, String tri, int page, int taille) {
        this(q, categorie, idCategorie, typeEspace, commune, region, idEspace, prixMin, prixMax, estProduit,
                avecPromotion, espaceVerifie, ouvertMaintenant, tri, page, taille, null, null, null,
                null, null, null, null, null, null);
    }

    public static CritereRecherche vide() {
        return new CritereRecherche(null, null, null, null, null, null, null, null, null, null, null, null, null, "PERTINENCE", 0, 12);
    }

    public static CritereRecherche parEspace(Long idEspace) {
        return new CritereRecherche(null, null, null, null, null, null, idEspace, null, null, null, null, null, null, "DATE_DESC", 0, 50);
    }
}
