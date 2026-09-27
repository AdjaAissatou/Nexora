package sn.ucad.nexora.recherche.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.recherche.application.dto.request.SignalerRequest;
import sn.ucad.nexora.recherche.application.dto.response.MessageResponse;
import sn.ucad.nexora.recherche.application.usecase.SignalementUseCase;
import sn.ucad.nexora.recherche.infrastructure.persistence.UtilisateurLookupRepository;

/**
 * Endpoints pour les signalements.
 *
 * POST /api/v1/signalements   — Signaler une offre ou un espace (AUTHENTIFIÉ)
 */
@RestController
@RequestMapping("/api/v1/signalements")
@Tag(name = "Signalements", description = "Signalement de contenu inapproprié")
public class SignalementController {

    private final SignalementUseCase signalementUseCase;
    private final UtilisateurLookupRepository lookupRepository;

    public SignalementController(SignalementUseCase signalementUseCase,
                                 UtilisateurLookupRepository lookupRepository) {
        this.signalementUseCase = signalementUseCase;
        this.lookupRepository = lookupRepository;
    }

    @PostMapping
    @Operation(summary = "Signaler une offre ou un espace",
               description = "Envoie un signalement à l'équipe de modération. Statut initial : EN_ATTENTE.")
    public ResponseEntity<MessageResponse> signaler(
            Authentication auth,
            @Valid @RequestBody SignalerRequest request) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        signalementUseCase.signaler(utilisateurId, request);
        return ResponseEntity.status(201)
                .body(new MessageResponse("Signalement enregistré, il sera traité dans les plus brefs délais"));
    }
}
