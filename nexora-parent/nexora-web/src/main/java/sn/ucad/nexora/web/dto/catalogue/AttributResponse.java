package sn.ucad.nexora.web.dto.catalogue;

import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.AttributResponse}. */
public record AttributResponse(
        Long id,
        String nom,
        String code,
        String typeChamp,
        boolean obligatoire,
        String unite,
        List<ValeurAttributResponse> valeurs) {}
