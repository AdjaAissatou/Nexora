package sn.ucad.nexora.web.dto.catalogue;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.ValeurAttributResponse}. */
/** {@code couleur} : pastille #RRGGBB d'une couleur, null sinon. */
public record ValeurAttributResponse(Long id, String valeur, String couleur) {}
