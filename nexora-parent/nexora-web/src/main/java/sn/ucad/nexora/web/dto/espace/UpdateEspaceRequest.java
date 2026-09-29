package sn.ucad.nexora.web.dto.espace;

import java.math.BigDecimal;
import java.util.List;

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
        String adresseComplete,
        BigDecimal latitude,
        BigDecimal longitude,
        String logo,
        String couverture,
        String registreCommerce,
        String numeroNinea,
        String numeroRccm,
        List<String> photos) {}
