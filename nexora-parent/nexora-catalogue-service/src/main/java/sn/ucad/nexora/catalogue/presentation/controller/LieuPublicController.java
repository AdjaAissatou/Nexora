package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.response.LieuPublicResponse;
import sn.ucad.nexora.catalogue.application.usecase.LieuPublicUseCase;

import java.util.List;

/**
 * Lieux publics du Sénégal (marchés, lieux de culte, hôpitaux, gares...) : repères de
 * recherche/carte, publics en lecture.
 */
@RestController
@RequestMapping("/api/v1/lieux-publics")
@Tag(name = "Catalogue - Lieux publics", description = "Repères géographiques publics (marchés, hôpitaux, gares...)")
public class LieuPublicController {

    private final LieuPublicUseCase lieuxPublics;

    public LieuPublicController(LieuPublicUseCase lieuxPublics) {
        this.lieuxPublics = lieuxPublics;
    }

    @GetMapping("/recherche")
    @Operation(summary = "Recherche de lieux publics par nom, commune ou type")
    public ResponseEntity<List<LieuPublicResponse>> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String commune,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limite) {
        return ResponseEntity.ok(lieuxPublics.rechercher(q, commune, type, limite));
    }
}
