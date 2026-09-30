package sn.ucad.nexora.recherche.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.recherche.application.dto.request.PublierAvisRequest;
import sn.ucad.nexora.recherche.application.dto.response.AvisResponse;
import sn.ucad.nexora.recherche.application.dto.response.MessageResponse;
import sn.ucad.nexora.recherche.application.usecase.AvisUseCase;
import sn.ucad.nexora.recherche.infrastructure.persistence.UtilisateurLookupRepository;

import java.util.List;

/**
 * Endpoints pour les avis.
 *
 * GET    /api/v1/avis/offre/{offreId}    — Avis d'une offre       (PUBLIC)
 * GET    /api/v1/avis/espace/{espaceId}  — Avis d'un espace       (PUBLIC)
 * POST   /api/v1/avis                    — Publier un avis         (AUTHENTIFIÉ)
 * DELETE /api/v1/avis/{id}               — Supprimer son avis      (AUTHENTIFIÉ)
 */
@RestController
@RequestMapping("/api/v1/avis")
@Tag(name = "Avis", description = "Avis et notations des offres et espaces")
public class AvisController {

    private final AvisUseCase avisUseCase;
    private final UtilisateurLookupRepository lookupRepository;
    private final sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository avisLecture;

    public AvisController(AvisUseCase avisUseCase,
                          UtilisateurLookupRepository lookupRepository,
                          sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository avisLecture) {
        this.avisUseCase = avisUseCase;
        this.lookupRepository = lookupRepository;
        this.avisLecture = avisLecture;
    }

    /** L'avis du compte connecté sur un espace, même masqué (avec le motif) ; 204 s'il n'en a pas. */
    @GetMapping("/mien")
    @Operation(summary = "Mon avis sur un espace")
    public ResponseEntity<sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository.MonAvis> mien(
            Authentication auth, @RequestParam Long espaceId) {
        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return avisLecture.monAvisSurEspace(utilisateurId, espaceId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/offre/{offreId}")
    @Operation(summary = "Avis d'une offre",
               description = "Liste tous les avis publiés sur une offre. Accessible sans authentification.")
    public ResponseEntity<List<AvisResponse>> parOffre(
            @Parameter(description = "Identifiant de l'offre")
            @PathVariable Long offreId) {
        return ResponseEntity.ok(avisUseCase.listerParOffre(offreId));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Un avis", description = "Un avis visible (non masqué). Accessible sans authentification.")
    public ResponseEntity<AvisResponse> un(@PathVariable Long id) {
        return ResponseEntity.ok(avisUseCase.visible(id));
    }

    @GetMapping("/espace/{espaceId}")
    @Operation(summary = "Avis d'un espace",
               description = "Liste tous les avis publiés sur un espace professionnel. Accessible sans authentification.")
    public ResponseEntity<List<AvisResponse>> parEspace(
            @Parameter(description = "Identifiant de l'espace")
            @PathVariable Long espaceId) {
        return ResponseEntity.ok(avisUseCase.listerParEspace(espaceId));
    }

    @PostMapping
    @Operation(summary = "Publier un avis",
               description = "Publie un avis (note 1-5 + commentaire) sur une offre ou un espace. Un seul avis par offre.")
    public ResponseEntity<AvisResponse> publier(
            Authentication auth,
            @Valid @RequestBody PublierAvisRequest request) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return ResponseEntity.status(201).body(avisUseCase.publier(utilisateurId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer son avis",
               description = "Supprime un avis publié par l'utilisateur connecté.")
    public ResponseEntity<MessageResponse> supprimer(
            Authentication auth,
            @PathVariable Long id) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        avisUseCase.supprimer(utilisateurId, id);
        return ResponseEntity.ok(new MessageResponse("Avis supprimé"));
    }
}
