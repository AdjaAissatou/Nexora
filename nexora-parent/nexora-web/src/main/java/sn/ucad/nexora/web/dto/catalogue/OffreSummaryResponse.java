package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.OffreSummaryResponse}. */
public record OffreSummaryResponse(
        Long id,
        String titre,
        BigDecimal prix,
        BigDecimal ancienPrix,
        String imagePrincipale,
        boolean disponible,
        boolean negociable,
        Long espaceId,
        String espaceNom,
        String espaceLogo,
        boolean espaceCertifie,
        boolean espaceVerifie,
        BigDecimal espaceNoteMoyenne,
        Integer espaceNombreAvis,
        String typeEspace,
        String categorie,
        String commune,
        String region,
        BigDecimal latitude,
        BigDecimal longitude,
        String promotionNom,
        String typeReduction,
        BigDecimal valeurReduction,
        // Liste de gestion du professionnel seulement (§9.9) ; PUBLIE en recherche publique
        String statut,
        String motifModeration,
        /** Ouvert en ce moment ; null si l'espace n'a pas renseigné ses horaires (§10). */
        Boolean espaceOuvertMaintenant,
        /** Distance au lieu recherché (km) ; null hors recherche autour d'un lieu. */
        Double distanceKm,
        /** Quantité vendue (commandes non annulées). */
        long nombreVentes,
        /** Consultations de la fiche. */
        Long vueCount,
        /** Popularité (§23) : vues dans Découvrir, j'aime (Découvrir), favoris, badge « 🔥 Populaire ». */
        long vuesDecouvrir,
        int nombreJaime,
        int nombreFavoris,
        boolean populaire) {

    public boolean suspendue() {
        return "SUSPENDU".equals(statut);
    }


    public boolean enPromotion() {
        return promotionNom != null && !promotionNom.isBlank();
    }

    public String localisationCourte() {
        return commune != null && !commune.isBlank() ? commune : (region != null ? region : "");
    }
}
