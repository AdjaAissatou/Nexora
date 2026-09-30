package sn.ucad.nexora.catalogue.presentation.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.response.moderation.ModerationOffreDtos.OffreModeree;
import sn.ucad.nexora.catalogue.application.dto.response.moderation.ModerationOffreDtos.PageOffres;
import sn.ucad.nexora.catalogue.application.service.moderation.ModerationOffreService;

/** Modération des offres : permission MODERER_OFFRES exigée par SecurityConfig (§9.9). */
@RestController
@RequestMapping("/api/v1/admin/offres")
public class ModerationOffreController {

    /** Corps d'une décision de modération. */
    public record MotifRequest(String motif) {}

    private final ModerationOffreService service;

    public ModerationOffreController(ModerationOffreService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageOffres> rechercher(@RequestParam(required = false) String recherche,
                                                 @RequestParam(required = false) String statut,
                                                 @RequestParam(required = false) Long idEspace,
                                                 @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(service.rechercher(recherche, statut, idEspace, page));
    }

    @PostMapping("/{id:\\d+}/suspendre")
    public ResponseEntity<OffreModeree> suspendre(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                  @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.suspendre(accountId, id, r.motif()));
    }

    @PostMapping("/{id:\\d+}/republier")
    public ResponseEntity<OffreModeree> republier(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                  @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.republier(accountId, id, r.motif()));
    }
}
