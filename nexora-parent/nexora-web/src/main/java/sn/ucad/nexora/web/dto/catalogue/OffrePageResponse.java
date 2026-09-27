package sn.ucad.nexora.web.dto.catalogue;

import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.OffrePageResponse}. */
public record OffrePageResponse(
        List<OffreSummaryResponse> contenu,
        int page,
        int taille,
        long total,
        int totalPages,
        boolean dernierePage) {

    public static OffrePageResponse vide() {
        return new OffrePageResponse(List.of(), 0, 20, 0, 0, true);
    }
}
