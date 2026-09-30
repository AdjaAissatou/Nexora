package sn.ucad.nexora.web.dto.verification;

import java.time.LocalDateTime;

/** Ligne d'une liste de demandes (files de l'agent, supervision, demandes précédentes). */
public record VerificationResumeResponse(
        Long id, Long espaceId, String espaceNom, String typeEspace, String commune,
        String demandeurNom, String statut, String motif, String agentNom, boolean urgent,
        LocalDateTime dateCreation, LocalDateTime dateSoumission,
        LocalDateTime datePriseEnCharge, LocalDateTime dateDecision) {}
