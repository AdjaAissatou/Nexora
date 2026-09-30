package sn.ucad.nexora.espace.presentation.controller;

import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.EtatVerificationResponse;
import sn.ucad.nexora.espace.application.service.verification.VerificationProfessionnelService;

/** Vérification côté professionnel, limitée aux espaces du compte connecté (docs/architecture-acteurs.md §8.6). */
@RestController
@RequestMapping("/api/v1/espaces/{espaceId}/verification")
public class VerificationProfessionnelController {

    private final VerificationProfessionnelService service;

    public VerificationProfessionnelController(VerificationProfessionnelService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<EtatVerificationResponse> etat(@AuthenticationPrincipal UUID accountId, @PathVariable Long espaceId) {
        return ResponseEntity.ok(service.etat(accountId, espaceId));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EtatVerificationResponse> deposer(@AuthenticationPrincipal UUID accountId,
                                                            @PathVariable Long espaceId,
                                                            @RequestParam("typeJustificatifId") Long typeJustificatifId,
                                                            @RequestParam("fichier") MultipartFile fichier) {
        try {
            return ResponseEntity.ok(service.deposerDocument(accountId, espaceId, typeJustificatifId,
                    fichier.getOriginalFilename(), fichier.getBytes()));
        } catch (IOException e) {
            throw new BusinessException("Lecture du fichier impossible");
        }
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<EtatVerificationResponse> retirerDocument(@AuthenticationPrincipal UUID accountId,
                                                                    @PathVariable Long espaceId,
                                                                    @PathVariable Long documentId) {
        return ResponseEntity.ok(service.retirerDocument(accountId, espaceId, documentId));
    }

    @PostMapping("/soumettre")
    public ResponseEntity<EtatVerificationResponse> soumettre(@AuthenticationPrincipal UUID accountId, @PathVariable Long espaceId) {
        return ResponseEntity.ok(service.soumettre(accountId, espaceId));
    }

    @PostMapping("/retirer")
    public ResponseEntity<EtatVerificationResponse> retirer(@AuthenticationPrincipal UUID accountId, @PathVariable Long espaceId) {
        return ResponseEntity.ok(service.retirer(accountId, espaceId));
    }
}
