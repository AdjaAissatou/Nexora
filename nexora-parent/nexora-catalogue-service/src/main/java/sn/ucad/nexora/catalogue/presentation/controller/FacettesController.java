package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.response.FacettesResponse;
import sn.ucad.nexora.catalogue.application.service.RechercherOffresService;
import sn.ucad.nexora.catalogue.infrastructure.persistence.FacettesQueryRepository;

/**
 * Facettes d'une recherche (§17) : mêmes paramètres que {@code GET /api/v1/offres/recherche} ;
 * renvoie les catégories et les valeurs de caractéristiques (tailles, couleurs…) présentes dans
 * les résultats, avec leurs comptes, pour affiner.
 */
@RestController
@Tag(name = "Catalogue - Recherche", description = "Facettes de recherche")
public class FacettesController {

    /** Au-delà, les facettes portent sur les offres les plus pertinentes. */
    private static final int MAX_OFFRES = 1000;

    private final RechercherOffresService recherche;
    private final FacettesQueryRepository facettes;

    public FacettesController(RechercherOffresService recherche, FacettesQueryRepository facettes) {
        this.recherche = recherche;
        this.facettes = facettes;
    }

    @GetMapping("/api/v1/offres/recherche/facettes")
    @Operation(summary = "Catégories et caractéristiques (tailles, couleurs…) présentes dans les résultats d'une recherche")
    public ResponseEntity<FacettesResponse> facettes(@ModelAttribute OffreSearchRequest request) {
        return ResponseEntity.ok(facettes.facettes(recherche.idsCorrespondants(request, MAX_OFFRES)));
    }
}
