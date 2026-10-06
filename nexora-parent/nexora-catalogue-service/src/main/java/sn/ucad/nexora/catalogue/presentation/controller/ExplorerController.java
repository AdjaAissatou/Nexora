package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse;
import sn.ucad.nexora.catalogue.infrastructure.persistence.ExplorerQueryRepository;

/** Page « Explorer » : catégories, quartiers, lieux connus et carte des espaces, avec leurs comptes. */
@RestController
@RequestMapping("/api/v1/explorer")
@Tag(name = "Catalogue - Explorer", description = "Vue d'ensemble de ce que propose la plateforme")
public class ExplorerController {

    private final ExplorerQueryRepository explorer;

    public ExplorerController(ExplorerQueryRepository explorer) {
        this.explorer = explorer;
    }

    @GetMapping
    @Operation(summary = "Catégories, quartiers, lieux publics et espaces, avec le nombre d'offres visibles")
    public ResponseEntity<ExplorerResponse> explorer() {
        return ResponseEntity.ok(explorer.explorer());
    }

    @GetMapping("/espaces")
    @Operation(summary = "Espaces dont le nom correspond au texte (sans accents ni espaces), même sans offre")
    public ResponseEntity<java.util.List<sn.ucad.nexora.catalogue.application.dto.response.EspaceTrouveResponse>> espaces(
            @org.springframework.web.bind.annotation.RequestParam String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "6") int limite) {
        return ResponseEntity.ok(explorer.espacesParNom(q, Math.max(1, Math.min(limite, 20))));
    }
}
