package sn.ucad.nexora.recherche.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.recherche.application.dto.request.EnregistrerConsultationRequest;
import sn.ucad.nexora.recherche.application.dto.request.EnregistrerRechercheRequest;
import sn.ucad.nexora.recherche.application.dto.response.HistoriqueRechercheResponse;
import sn.ucad.nexora.recherche.application.dto.response.MessageResponse;
import sn.ucad.nexora.recherche.application.usecase.ConsultationUseCase;
import sn.ucad.nexora.recherche.application.usecase.HistoriqueRechercheUseCase;
import sn.ucad.nexora.recherche.infrastructure.persistence.UtilisateurLookupRepository;

import java.util.List;

/**
 * Endpoints pour l'historique de recherche et de consultation.
 *
 * POST   /api/v1/historique/recherches          — Enregistrer une recherche
 * GET    /api/v1/historique/recherches          — Lister mes recherches récentes
 * DELETE /api/v1/historique/recherches          — Effacer mon historique de recherche
 *
 * POST   /api/v1/historique/consultations       — Enregistrer une consultation
 * DELETE /api/v1/historique/consultations       — Effacer mon historique de consultation
 */
@RestController
@RequestMapping("/api/v1/historique")
@Tag(name = "Historique", description = "Historique de recherche et de consultation")
public class HistoriqueController {

    private final HistoriqueRechercheUseCase historiqueRechercheUseCase;
    private final ConsultationUseCase consultationUseCase;
    private final UtilisateurLookupRepository lookupRepository;

    public HistoriqueController(HistoriqueRechercheUseCase historiqueRechercheUseCase,
                                ConsultationUseCase consultationUseCase,
                                UtilisateurLookupRepository lookupRepository) {
        this.historiqueRechercheUseCase = historiqueRechercheUseCase;
        this.consultationUseCase = consultationUseCase;
        this.lookupRepository = lookupRepository;
    }

    // ─── Historique de recherche ───────────────────────────────────────────

    @PostMapping("/recherches")
    @Operation(summary = "Enregistrer une recherche effectuée",
               description = "Appelé automatiquement par le front après chaque recherche pour tracer l'historique")
    public ResponseEntity<Void> enregistrerRecherche(
            Authentication auth,
            @RequestBody EnregistrerRechercheRequest request) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        historiqueRechercheUseCase.enregistrer(utilisateurId, request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/recherches")
    @Operation(summary = "Mes recherches récentes",
               description = "Retourne les dernières recherches de l'utilisateur connecté, du plus récent au plus ancien")
    public ResponseEntity<List<HistoriqueRechercheResponse>> listerRecherches(Authentication auth) {
        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return ResponseEntity.ok(historiqueRechercheUseCase.lister(utilisateurId));
    }

    @DeleteMapping("/recherches")
    @Operation(summary = "Effacer tout l'historique de recherche")
    public ResponseEntity<MessageResponse> effacerRecherches(Authentication auth) {
        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        historiqueRechercheUseCase.effacer(utilisateurId);
        return ResponseEntity.ok(new MessageResponse("Historique de recherche effacé"));
    }

    // ─── Historique de consultation ────────────────────────────────────────

    @PostMapping("/consultations")
    @Operation(summary = "Enregistrer la consultation d'une offre ou d'un espace",
               description = "Appelé quand l'utilisateur ouvre la fiche d'une offre ou d'un espace")
    public ResponseEntity<Void> enregistrerConsultation(
            Authentication auth,
            @RequestBody EnregistrerConsultationRequest request) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        consultationUseCase.enregistrer(utilisateurId, request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/consultations")
    @Operation(summary = "Effacer l'historique de consultation")
    public ResponseEntity<MessageResponse> effacerConsultations(Authentication auth) {
        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        consultationUseCase.effacer(utilisateurId);
        return ResponseEntity.ok(new MessageResponse("Historique de consultation effacé"));
    }
}
