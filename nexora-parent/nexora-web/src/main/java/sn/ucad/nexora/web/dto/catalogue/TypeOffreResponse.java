package sn.ucad.nexora.web.dto.catalogue;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.TypeOffreResponse}. */
public record TypeOffreResponse(
        Long id,
        String libelle,
        String description,
        String principale) {}
