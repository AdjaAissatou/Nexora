package sn.ucad.nexora.web.client;

import java.math.BigDecimal;

/**
 * Critères de recherche d'offres, tels qu'attendus par
 * {@code GET /api/v1/offres/recherche} de catalogue-service. Tous les champs sont optionnels.
 */
public record CritereRecherche(
        String q,
        String categorie,
        String typeEspace,
        String commune,
        String region,
        Long idEspace,
        BigDecimal prixMin,
        BigDecimal prixMax,
        Boolean estProduit,
        Boolean avecPromotion,
        Boolean espaceVerifie,
        String tri,
        int page,
        int taille) {

    public static CritereRecherche vide() {
        return new CritereRecherche(null, null, null, null, null, null, null, null, null, null, null, "PERTINENCE", 0, 12);
    }

    public static CritereRecherche parEspace(Long idEspace) {
        return new CritereRecherche(null, null, null, null, null, idEspace, null, null, null, null, null, "DATE_DESC", 0, 50);
    }
}
