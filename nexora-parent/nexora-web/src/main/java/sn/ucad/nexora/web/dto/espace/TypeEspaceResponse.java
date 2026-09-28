package sn.ucad.nexora.web.dto.espace;

/** Miroir de {@code sn.ucad.nexora.espace.application.dto.response.TypeEspaceResponse}. */
public record TypeEspaceResponse(
        Long id, String nom, String description, String icone, String couleur, Integer ordreAffichage) {}
