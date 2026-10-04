package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.request.AttributValeurRequest}. */
public record AttributValeurRequest(
        Long idAttribut,
        String valeurTexte,
        BigDecimal valeurNombre,
        String valeurDate,
        Long idValeur,
        boolean epuise) {}
