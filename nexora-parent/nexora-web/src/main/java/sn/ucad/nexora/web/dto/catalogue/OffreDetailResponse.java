package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse}. */
public record OffreDetailResponse(
        Long id,
        String titre,
        String description,
        BigDecimal prix,
        BigDecimal ancienPrix,
        boolean negociable,
        boolean disponible,
        boolean estCommandable,
        boolean estReservable,
        boolean stockable,
        Integer quantiteDisponible,
        Long vueCount,
        String statut,
        LocalDateTime datePublication,
        Long categorieId,
        String categorie,
        String typeOffre,
        Long espaceId,
        String espaceNom,
        String espaceSlogan,
        String espaceLogo,
        String espaceCouverture,
        String espaceTelephone,
        String espaceEmail,
        String espaceSiteWeb,
        boolean espaceCertifie,
        boolean espaceVerifie,
        boolean espaceOuvert,
        BigDecimal espaceNoteMoyenne,
        Integer espaceNombreAvis,
        Long espaceNombreVues,
        Long espaceNombreFavoris,
        String typeEspace,
        Localisation localisation,
        Promotion promotion,
        List<String> images,
        String imagePrincipale,
        Produit produit,
        Service service,
        List<String> tags,
        List<Caracteristique> caracteristiques,
        /** Popularité (§23) : vues dans Découvrir, j'aime (Découvrir), favoris, badge « 🔥 Populaire ». */
        long vuesDecouvrir,
        int nombreJaime,
        int nombreFavoris,
        boolean populaire) {

    public boolean enPromotion() {
        return promotion != null;
    }

    public record Localisation(
            String pays,
            String region,
            String departement,
            String commune,
            String arrondissement,
            String quartier,
            String adresseComplete,
            String codePostal,
            BigDecimal latitude,
            BigDecimal longitude) {

        public String texte() {
            return java.util.stream.Stream.of(quartier, commune, region)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }
    }

    /** Caractéristique renseignée : « Tailles disponibles » → S, M, L… */
    public record Caracteristique(String nom, String typeChamp, String unite, List<Valeur> valeurs) {

        public boolean multiple() {
            return "MULTI_LISTE".equals(typeChamp);
        }

        /** Au moins une valeur momentanément épuisée (affichée barrée). */
        public boolean avecEpuises() {
            return valeurs != null && valeurs.stream().anyMatch(Valeur::epuise);
        }
    }

    public record Valeur(String libelle, String couleur, boolean epuise) {}

    public record Promotion(String nom, String typeReduction, BigDecimal valeur, LocalDateTime dateFin) {}

    public record Produit(
            String marque,
            String modele,
            String reference,
            Integer quantiteStock,
            Double poids,
            String garantie,
            boolean neuf) {}

    public record Service(
            Integer dureeEstimee,
            boolean interventionDomicile,
            boolean interventionDistance,
            Integer delaiReponse,
            boolean reservation,
            boolean urgence) {}
}
