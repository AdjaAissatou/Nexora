package sn.ucad.nexora.espace.presentation.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.espace.application.dto.request.verification.VerificationRequests.MotifRequest;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.FicheEspace;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.PageEspaces;
import sn.ucad.nexora.espace.application.service.moderation.ModerationEspaceService;

/** Modération des espaces : permission MODERER_ESPACES exigée par SecurityConfig (§9.9). */
@RestController
@RequestMapping("/api/v1/admin/espaces")
public class ModerationEspaceController {

    private final ModerationEspaceService service;

    public ModerationEspaceController(ModerationEspaceService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageEspaces> rechercher(@RequestParam(required = false) String recherche,
                                                  @RequestParam(required = false) String statut,
                                                  @RequestParam(required = false) Boolean verifie,
                                                  @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(service.rechercher(recherche, statut, verifie, page));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<FicheEspace> fiche(@PathVariable Long id) {
        return ResponseEntity.ok(service.fiche(id));
    }

    @PostMapping("/{id:\\d+}/suspendre")
    public ResponseEntity<FicheEspace> suspendre(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                 @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.suspendre(accountId, id, r.motif()));
    }

    @PostMapping("/{id:\\d+}/reactiver")
    public ResponseEntity<FicheEspace> reactiver(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                 @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.reactiver(accountId, id, r.motif()));
    }
}
