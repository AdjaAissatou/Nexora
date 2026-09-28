package sn.ucad.nexora.web.dto.espace;

/** Miroir de {@code sn.ucad.nexora.espace.application.dto.request.UpdateEspaceRequest}. */
public record UpdateEspaceRequest(
        String nom,
        String slogan,
        String description,
        String telephone,
        String telephoneSecondaire,
        String email,
        String siteWeb,
        Boolean ouvert,
        Long idRegion,
        Long idDepartement,
        Long idCommune,
        String quartier,
        String adresseComplete) {}
