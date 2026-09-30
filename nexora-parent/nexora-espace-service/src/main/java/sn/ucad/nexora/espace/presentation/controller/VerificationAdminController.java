package sn.ucad.nexora.espace.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.espace.application.dto.request.verification.VerificationRequests.MotifRequest;
import sn.ucad.nexora.espace.application.dto.request.verification.VerificationRequests.ReattributionRequest;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.StatistiquesVerificationResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationDetailResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationResumeResponse;
import sn.ucad.nexora.espace.application.service.verification.VerificationAdminService;

/** Supervision des vérifications. Rôle ADMIN ou SUPER_ADMIN exigé par SecurityConfig. */
@RestController
@RequestMapping("/api/v1/admin/verifications")
public class VerificationAdminController {

    private final VerificationAdminService service;

    public VerificationAdminController(VerificationAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VerificationResumeResponse>> toutes(@RequestParam(required = false) String statut) {
        return ResponseEntity.ok(service.toutes(statut));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesVerificationResponse> statistiques() {
        return ResponseEntity.ok(service.statistiques());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VerificationDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.detail(id));
    }

    @PostMapping("/{id}/reattribuer")
    public ResponseEntity<VerificationDetailResponse> reattribuer(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                                  @RequestBody ReattributionRequest r) {
        return ResponseEntity.ok(service.reattribuer(accountId, id, r.agentUtilisateurId(), r.motif()));
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<VerificationDetailResponse> annuler(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                              @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.annuler(accountId, id, r.motif()));
    }

    @PostMapping("/{id}/revoquer")
    public ResponseEntity<VerificationDetailResponse> revoquer(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                               @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.revoquer(accountId, id, r.motif()));
    }
}
