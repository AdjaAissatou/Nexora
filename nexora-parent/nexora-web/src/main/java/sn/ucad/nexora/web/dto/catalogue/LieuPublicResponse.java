package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.LieuPublicResponse}. */
public record LieuPublicResponse(
        Long id,
        String nom,
        String typeLieu,
        String region,
        String departement,
        String commune,
        String adresseComplete,
        BigDecimal latitude,
        BigDecimal longitude,
        String description) {}
