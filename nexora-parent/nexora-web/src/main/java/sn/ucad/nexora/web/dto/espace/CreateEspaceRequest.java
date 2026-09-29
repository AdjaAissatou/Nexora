package sn.ucad.nexora.web.dto.espace;

import java.math.BigDecimal;
import java.util.List;

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
        String numeroRccm,
        Long idRegion,
        Long idDepartement,
        Long idCommune,
        String quartier,
        String adresseComplete,
        BigDecimal latitude,
        BigDecimal longitude,
        String logo,
        String couverture,
        List<String> photos) {}
