package sn.ucad.nexora.administration.presentation.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.administration.application.service.parametre.ParametresService;
import sn.ucad.nexora.administration.application.service.parametre.ParametresService.ModificationRequest;
import sn.ucad.nexora.administration.application.service.parametre.ParametresService.Parametre;

/** Paramètres de Nexora (§9.12) : lecture publique restreinte, gestion avec GERER_PARAMETRES. */
@RestController
@RequestMapping("/api/v1")
public class ParametresController {

    private final ParametresService service;

    public ParametresController(ParametresService service) {
        this.service = service;
    }

    @GetMapping("/public/parametres")
    public ResponseEntity<Map<String, String>> publics() {
        return ResponseEntity.ok(service.publics());
    }

    @GetMapping("/admin/parametres")
    public ResponseEntity<List<Parametre>> tous() {
        return ResponseEntity.ok(service.tous());
    }

    @PutMapping("/admin/parametres/{code}")
    public ResponseEntity<Parametre> modifier(@AuthenticationPrincipal UUID compte, @PathVariable String code,
                                              @RequestBody ModificationRequest r) {
        return ResponseEntity.ok(service.modifier(compte, code, r.valeur(), r.motif()));
    }
}
