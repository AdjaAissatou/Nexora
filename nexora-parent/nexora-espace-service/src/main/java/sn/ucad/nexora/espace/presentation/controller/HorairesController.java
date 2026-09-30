package sn.ucad.nexora.espace.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.ExceptionRequest;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.HorairesResponse;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.SemaineRequest;
import sn.ucad.nexora.espace.application.service.horaire.HorairesService;

/**
 * Horaires d'un espace (§10) : lecture publique (jeton facultatif), modification par le propriétaire
 * avec la permission GERER_HORAIRES (SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/espaces/{id:\\d+}/horaires")
public class HorairesController {

    private final HorairesService service;

    public HorairesController(HorairesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<HorairesResponse> lire(@PathVariable Long id, Authentication auth) {
        UUID compte = auth != null && auth.getPrincipal() instanceof UUID u ? u : null;
        List<String> autorites = auth == null ? List.of() : auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return ResponseEntity.ok(service.lire(id, compte, autorites));
    }

    @PutMapping
    public ResponseEntity<HorairesResponse> semaine(@AuthenticationPrincipal UUID compte, @PathVariable Long id,
                                                    @RequestBody SemaineRequest r) {
        return ResponseEntity.ok(service.enregistrerSemaine(compte, id, r.semaine()));
    }

    @PostMapping("/exceptions")
    public ResponseEntity<HorairesResponse> exception(@AuthenticationPrincipal UUID compte, @PathVariable Long id,
                                                      @RequestBody ExceptionRequest r) {
        return ResponseEntity.ok(service.enregistrerException(compte, id, r));
    }

    @DeleteMapping("/exceptions/{exception:\\d+}")
    public ResponseEntity<HorairesResponse> supprimer(@AuthenticationPrincipal UUID compte, @PathVariable Long id,
                                                      @PathVariable Long exception) {
        return ResponseEntity.ok(service.supprimerException(compte, id, exception));
    }
}
