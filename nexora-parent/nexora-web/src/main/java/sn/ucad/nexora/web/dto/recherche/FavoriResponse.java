package sn.ucad.nexora.web.dto.recherche;

import java.time.LocalDateTime;

/** Miroir de {@code sn.ucad.nexora.recherche.application.dto.response.FavoriResponse}. */
public record FavoriResponse(
        Long id,
        Long offreId,
        Long espaceId,
        LocalDateTime dateCreation) {}
