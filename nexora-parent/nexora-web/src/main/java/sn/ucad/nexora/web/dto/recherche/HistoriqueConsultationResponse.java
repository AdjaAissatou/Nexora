package sn.ucad.nexora.web.dto.recherche;

import java.time.LocalDateTime;

/** Miroir de {@code sn.ucad.nexora.recherche.application.dto.response.HistoriqueConsultationResponse}. */
public record HistoriqueConsultationResponse(
        Long id,
        Long offreId,
        Long espaceId,
        LocalDateTime dateConsultation) {}
