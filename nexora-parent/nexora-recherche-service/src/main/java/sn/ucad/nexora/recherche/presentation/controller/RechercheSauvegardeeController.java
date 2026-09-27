package sn.ucad.nexora.recherche.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.recherche.application.dto.request.SauvegarderRechercheRequest;
import sn.ucad.nexora.recherche.application.dto.response.MessageResponse;
import sn.ucad.nexora.recherche.application.dto.response.RechercheSauvegardeeResponse;
import sn.ucad.nexora.recherche.application.usecase.RechercheSauvegardeeUseCase;
import sn.ucad.nexora.recherche.infrastructure.persistence.UtilisateurLookupRepository;

import java.util.List;

/**
 * Endpoints pour les recherches sauvegardées (alertes).
 *
 * GET    /api/v1/recherches-sauvegardees        — Mes recherches sauvegardées
 * POST   /api/v1/recherches-sauvegardees        — Sauvegarder une recherche
 * DELETE /api/v1/recherches-sauvegardees/{id}   — Supprimer une recherche sauvegardée
 */
@RestController
@RequestMapping("/api/v1/recherches-sauvegardees")
@Tag(name = "Recherches sauvegardées", description = "Alertes et recherches favorites de l'utilisateur")
public class RechercheSauvegardeeController {

    private final RechercheSauvegardeeUseCase useCase;
    private final UtilisateurLookupRepository lookupRepository;

    public RechercheSauvegardeeController(RechercheSauvegardeeUseCase useCase,
                                          UtilisateurLookupRepository lookupRepository) {
        this.useCase = useCase;
        this.lookupRepository = lookupRepository;
    }

    @GetMapping
    @Operation(summary = "Mes recherches sauvegardées")
    public ResponseEntity<List<RechercheSauvegardeeResponse>> lister(Authentication auth) {
        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return ResponseEntity.ok(useCase.lister(utilisateurId));
    }

    @PostMapping
    @Operation(summary = "Sauvegarder une recherche",
               description = "Enregistre les critères d'une recherche pour la retrouver ou recevoir des alertes")
    public ResponseEntity<RechercheSauvegardeeResponse> sauvegarder(
            Authentication auth,
            @Valid @RequestBody SauvegarderRechercheRequest request) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return ResponseEntity.status(201).body(useCase.sauvegarder(utilisateurId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une recherche sauvegardée")
    public ResponseEntity<MessageResponse> supprimer(
            Authentication auth,
            @PathVariable Long id) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        useCase.supprimer(utilisateurId, id);
        return ResponseEntity.ok(new MessageResponse("Recherche supprimée"));
    }
}
