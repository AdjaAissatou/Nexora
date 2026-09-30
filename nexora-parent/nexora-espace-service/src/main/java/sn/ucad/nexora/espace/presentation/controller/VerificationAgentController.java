package sn.ucad.nexora.espace.presentation.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.espace.application.dto.request.verification.VerificationRequests.ControleRequest;
import sn.ucad.nexora.espace.application.dto.request.verification.VerificationRequests.ExamenDocumentRequest;
import sn.ucad.nexora.espace.application.dto.request.verification.VerificationRequests.MotifRequest;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationDetailResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationResumeResponse;
import sn.ucad.nexora.espace.application.service.verification.JustificatifAccesService;
import sn.ucad.nexora.espace.application.service.verification.VerificationAgentService;

/**
 * Espace de travail de l'agent de vérification. Rôle AGENT_VERIFICATION exigé par SecurityConfig,
 * sauf pour le téléchargement d'un justificatif, dont l'accès est contrôlé document par document.
 */
@RestController
@RequestMapping("/api/v1/verifications")
public class VerificationAgentController {

    private final VerificationAgentService service;
    private final JustificatifAccesService acces;

    public VerificationAgentController(VerificationAgentService service, JustificatifAccesService acces) {
        this.service = service;
        this.acces = acces;
    }

    @GetMapping
    public ResponseEntity<List<VerificationResumeResponse>> file(@AuthenticationPrincipal UUID accountId,
                                                                 @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(service.file(accountId, statut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VerificationDetailResponse> detail(@AuthenticationPrincipal UUID accountId, @PathVariable Long id) {
        return ResponseEntity.ok(service.detail(accountId, id));
    }

    @PostMapping("/{id}/prendre")
    public ResponseEntity<VerificationDetailResponse> prendre(@AuthenticationPrincipal UUID accountId, @PathVariable Long id) {
        return ResponseEntity.ok(service.prendre(accountId, id));
    }

    @PutMapping("/{id}/controles/{code}")
    public ResponseEntity<VerificationDetailResponse> controler(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                                @PathVariable String code, @RequestBody ControleRequest r) {
        return ResponseEntity.ok(service.controler(accountId, id, code, r.resultat(), r.commentaire()));
    }

    @PutMapping("/{id}/documents/{documentId}/examen")
    public ResponseEntity<VerificationDetailResponse> examiner(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                               @PathVariable Long documentId, @RequestBody ExamenDocumentRequest r) {
        return ResponseEntity.ok(service.examinerDocument(accountId, id, documentId, r.decision(), r.motif()));
    }

    @PostMapping("/{id}/demander-infos")
    public ResponseEntity<VerificationDetailResponse> demanderInfos(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                                    @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.demanderInformations(accountId, id, r.motif()));
    }

    @PostMapping("/{id}/approuver")
    public ResponseEntity<VerificationDetailResponse> approuver(@AuthenticationPrincipal UUID accountId, @PathVariable Long id) {
        return ResponseEntity.ok(service.approuver(accountId, id));
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<VerificationDetailResponse> refuser(@AuthenticationPrincipal UUID accountId, @PathVariable Long id,
                                                              @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.refuser(accountId, id, r.motif()));
    }

    /** Téléchargement contrôlé : jamais d'URL publique pour un justificatif. */
    @GetMapping("/{id}/documents/{documentId}/fichier")
    public ResponseEntity<InputStreamResource> fichier(Authentication auth, @PathVariable Long id, @PathVariable Long documentId) {
        boolean agent = aLeRole(auth, "ROLE_AGENT_VERIFICATION");
        boolean admin = aLeRole(auth, "ROLE_ADMIN") || aLeRole(auth, "ROLE_SUPER_ADMIN");
        JustificatifAccesService.Fichier f = acces.ouvrir((UUID) auth.getPrincipal(), agent, admin, id, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.typeMime()))
                .contentLength(f.taille())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(f.nom(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(f.contenu()));
    }

    private static boolean aLeRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
