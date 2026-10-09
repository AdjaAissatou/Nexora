package sn.ucad.nexora.web.dto.espace;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.espace.application.dto.response.EspaceResponse}. */
public record EspaceResponse(
        Long id,
        Long utilisateurId,
        Long typeEspaceId,
        String nom,
        String slogan,
        String description,
        String telephone,
        String telephoneSecondaire,
        String email,
        String siteWeb,
        String logo,
        String couverture,
        boolean ouvert,
        String statut,
        boolean certifie,
        boolean verifie,
        BigDecimal noteMoyenne,
        Integer nombreAvis,
        Long nombreVues,
        Long nombreFavoris,
        LocalDateTime dateCreation,
        String pays,
        String region,
        String departement,
        String commune,
        String quartier,
        String adresseComplete,
        BigDecimal latitude,
        BigDecimal longitude,
        String registreCommerce,
        String numeroNinea,
        String numeroRccm,
        List<String> photos,
        // Modération (§9.9) : renseignés pour le propriétaire quand l'espace est suspendu
        String motifModeration,
        java.time.LocalDateTime dateModeration,
        /** J'aime sur la fiche de l'espace dans Découvrir (§23). */
        Integer nombreJaime) {

    public boolean suspendu() {
        return "SUSPENDU".equals(statut);
    }
}
