package sn.ucad.nexora.espace.presentation.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.espace.application.service.justificatif.ReglesJustificatifsService;
import sn.ucad.nexora.espace.application.service.justificatif.ReglesJustificatifsService.*;

/** Règles des justificatifs de la vérification (§9.12) : permission GERER_PARAMETRES (SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin/justificatifs")
public class ReglesJustificatifsController {

    private final ReglesJustificatifsService service;

    public ReglesJustificatifsController(ReglesJustificatifsService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Vue> vue() {
        return ResponseEntity.ok(service.vue());
    }

    @PostMapping("/types")
    public ResponseEntity<Vue> creerType(@AuthenticationPrincipal UUID c, @RequestBody TypeRequest r) {
        return ResponseEntity.ok(service.creerType(c, r));
    }

    @PutMapping("/types/{id:\\d+}")
    public ResponseEntity<Vue> modifierType(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody TypeRequest r) {
        return ResponseEntity.ok(service.modifierType(c, id, r));
    }

    @PostMapping("/types/{id:\\d+}/{action:activer|desactiver}")
    public ResponseEntity<Vue> activerType(@AuthenticationPrincipal UUID c, @PathVariable Long id, @PathVariable String action,
                                           @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.activerType(c, id, "activer".equals(action), r.motif()));
    }

    @PostMapping("/regles")
    public ResponseEntity<Vue> ajouterRegle(@AuthenticationPrincipal UUID c, @RequestBody RegleRequest r) {
        return ResponseEntity.ok(service.ajouterRegle(c, r));
    }

    @PutMapping("/regles/{id:\\d+}")
    public ResponseEntity<Vue> modifierRegle(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody RegleRequest r) {
        return ResponseEntity.ok(service.modifierRegle(c, id, r));
    }

    @PostMapping("/regles/{id:\\d+}/supprimer")
    public ResponseEntity<Vue> supprimerRegle(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.supprimerRegle(c, id, r.motif()));
    }
}
