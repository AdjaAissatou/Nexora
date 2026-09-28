package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.response.AttributResponse;
import sn.ucad.nexora.catalogue.application.dto.response.CategorieResponse;
import sn.ucad.nexora.catalogue.application.dto.response.TypeOffreResponse;
import sn.ucad.nexora.catalogue.application.usecase.CatalogueMetaUseCase;

import java.util.List;

/**
 * Navigation du catalogue : catégories, sous-catégories, types d'offre et attributs.
 * Public en lecture — alimente le formulaire "créer une offre" côté web et les filtres de recherche.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Catalogue - Catégories", description = "Hiérarchie de catégories, types d'offre et attributs")
public class CategorieController {

    private final CatalogueMetaUseCase meta;

    public CategorieController(CatalogueMetaUseCase meta) {
        this.meta = meta;
    }

    @GetMapping
    @Operation(summary = "Catégories racines")
    public ResponseEntity<List<CategorieResponse>> racines() {
        return ResponseEntity.ok(meta.categoriesRacines());
    }

    @GetMapping("/{id}/sous-categories")
    @Operation(summary = "Sous-catégories d'une catégorie")
    public ResponseEntity<List<CategorieResponse>> sousCategories(@PathVariable Long id) {
        return ResponseEntity.ok(meta.sousCategories(id));
    }

    @GetMapping("/{id}/types-offre")
    @Operation(summary = "Types d'offre (produit/service) d'une catégorie")
    public ResponseEntity<List<TypeOffreResponse>> typesOffre(@PathVariable Long id) {
        return ResponseEntity.ok(meta.typesOffre(id));
    }

    @GetMapping("/{id}/attributs")
    @Operation(summary = "Attributs (listes déroulantes) d'une catégorie")
    public ResponseEntity<List<AttributResponse>> attributs(@PathVariable Long id) {
        return ResponseEntity.ok(meta.attributs(id));
    }
}
