package sn.ucad.nexora.web.dto.verification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Miroir de {@code VerificationDtos.EtatVerificationResponse} (espace-service) : état de vérification
 * d'un espace vu par son propriétaire. {@code etat} : NON_VERIFIE, EN_COURS, A_COMPLETER, VERIFIE,
 * REFUSEE ou REVOQUEE.
 */
public record EtatVerificationResponse(
        Long espaceId,
        String etat,
        boolean verifie,
        LocalDateTime dateVerification,
        boolean peutDeposerDocuments,
        boolean peutSoumettre,
        boolean peutRetirer,
        List<ElementCompletude> completude,
        List<JustificatifAttendu> justificatifsAttendus,
        VerificationDetailResponse demande,
        List<VerificationResumeResponse> demandesPrecedentes) {}
