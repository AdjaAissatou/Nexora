package sn.ucad.nexora.web.dto.catalogue;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.CategorieResponse}. */
public record CategorieResponse(
        Long id,
        Long idParent,
        String nom,
        String description,
        String icone,
        String couleur,
        boolean aDesEnfants) {}
