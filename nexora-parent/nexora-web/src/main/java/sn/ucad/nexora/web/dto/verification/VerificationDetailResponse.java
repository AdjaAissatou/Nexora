package sn.ucad.nexora.web.dto.verification;

import java.util.List;

/** Détail d'une demande : espace examiné, complétude, documents, contrôles, historique, blocages. */
public record VerificationDetailResponse(
        VerificationResumeResponse resume,
        EspaceExamine espace,
        List<ElementCompletude> completude,
        List<DocumentVerificationResponse> documents,
        List<ControleResponse> controles,
        List<EvenementVerificationResponse> historique,
        List<String> pointsBloquants) {}
