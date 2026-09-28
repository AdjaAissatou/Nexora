package sn.ucad.nexora.web.dto.espace;

/** Miroir de {@code sn.ucad.nexora.espace.application.dto.request.CreateEspaceRequest}. */
public record CreateEspaceRequest(
        Long typeEspaceId,
        String nom,
        String slogan,
        String description,
        String telephone,
        String telephoneSecondaire,
        String email,
        String siteWeb,
        String registreCommerce,
        String numeroNinea,
        String numeroRccm) {}
