package sn.ucad.nexora.recherche.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.recherche.application.dto.request.AjouterFavoriRequest;
import sn.ucad.nexora.recherche.application.dto.response.FavoriResponse;
import sn.ucad.nexora.recherche.application.dto.response.MessageResponse;
import sn.ucad.nexora.recherche.application.usecase.FavoriUseCase;
import sn.ucad.nexora.recherche.infrastructure.persistence.UtilisateurLookupRepository;

import java.util.List;

/**
 * Endpoints pour la gestion des favoris.
 *
 * GET    /api/v1/favoris                        — Mes favoris
 * POST   /api/v1/favoris                        — Ajouter un favori (offre ou espace)
 * DELETE /api/v1/favoris?offreId=X              — Retirer un favori (offre)
 * DELETE /api/v1/favoris?espaceId=X             — Retirer un favori (espace)
 */
@RestController
@RequestMapping("/api/v1/favoris")
@Tag(name = "Favoris", description = "Gestion des offres et espaces mis en favoris")
public class FavoriController {

    private final FavoriUseCase favoriUseCase;
    private final UtilisateurLookupRepository lookupRepository;

    public FavoriController(FavoriUseCase favoriUseCase,
                            UtilisateurLookupRepository lookupRepository) {
        this.favoriUseCase = favoriUseCase;
        this.lookupRepository = lookupRepository;
    }

    @GetMapping
    @Operation(summary = "Mes favoris", description = "Liste toutes les offres et espaces mis en favoris")
    public ResponseEntity<List<FavoriResponse>> lister(Authentication auth) {
        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return ResponseEntity.ok(favoriUseCase.lister(utilisateurId));
    }

    @PostMapping
    @Operation(summary = "Ajouter un favori",
               description = "Ajoute une offre (offreId) ou un espace (espaceId) aux favoris. Opération idempotente.")
    public ResponseEntity<FavoriResponse> ajouter(
            Authentication auth,
            @RequestBody AjouterFavoriRequest request) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        return ResponseEntity.status(201).body(favoriUseCase.ajouter(utilisateurId, request));
    }

    @DeleteMapping
    @Operation(summary = "Retirer un favori",
               description = "Retire une offre ou un espace des favoris. Passer offreId OU espaceId en query param.")
    public ResponseEntity<MessageResponse> supprimer(
            Authentication auth,
            @RequestParam(required = false) Long offreId,
            @RequestParam(required = false) Long espaceId) {

        Long utilisateurId = PrincipalHelper.resolveUtilisateurId(auth, lookupRepository);
        favoriUseCase.supprimer(utilisateurId, offreId, espaceId);
        return ResponseEntity.ok(new MessageResponse("Favori supprimé"));
    }
}
