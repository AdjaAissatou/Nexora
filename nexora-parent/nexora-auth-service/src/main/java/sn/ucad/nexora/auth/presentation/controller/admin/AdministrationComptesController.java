package sn.ucad.nexora.auth.presentation.controller.admin;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.FicheCompte;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.MotifRequest;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.PageComptes;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.RolesEtPermissions;
import sn.ucad.nexora.auth.application.service.admin.AdministrationComptesService;

/**
 * Administration des comptes, des rôles et des permissions. Chaque route exige une permission
 * (SecurityConfig) : CONSULTER_UTILISATEURS, SUSPENDRE_UTILISATEURS, REACTIVER_UTILISATEURS,
 * GERER_ROLES, GERER_PERMISSIONS, ACCEDER_BACK_OFFICE (lecture de la matrice).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdministrationComptesController {

    private final AdministrationComptesService service;

    public AdministrationComptesController(AdministrationComptesService service) {
        this.service = service;
    }

    @GetMapping("/comptes")
    public ResponseEntity<PageComptes> rechercher(@RequestParam(required = false) String recherche,
                                                  @RequestParam(required = false) String role,
                                                  @RequestParam(required = false) String etat,
                                                  @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(service.rechercher(recherche, role, etat, page));
    }

    @GetMapping("/comptes/{accountId}")
    public ResponseEntity<FicheCompte> fiche(@PathVariable UUID accountId) {
        return ResponseEntity.ok(service.fiche(accountId));
    }

    @PostMapping("/comptes/{accountId}/suspendre")
    public ResponseEntity<FicheCompte> suspendre(Authentication auth, @PathVariable UUID accountId,
                                                 @RequestBody MotifRequest r) {
        boolean superAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        return ResponseEntity.ok(service.suspendre((UUID) auth.getPrincipal(), superAdmin, accountId, r.motif()));
    }

    @PostMapping("/comptes/{accountId}/reactiver")
    public ResponseEntity<FicheCompte> reactiver(@AuthenticationPrincipal UUID auteur, @PathVariable UUID accountId,
                                                 @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.reactiver(auteur, accountId, r.motif()));
    }

    @PostMapping("/comptes/{accountId}/roles/{role}")
    public ResponseEntity<FicheCompte> donnerRole(@AuthenticationPrincipal UUID auteur, @PathVariable UUID accountId,
                                                  @PathVariable String role, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.donnerRole(auteur, accountId, role, r.motif()));
    }

    @PostMapping("/comptes/{accountId}/roles/{role}/retirer")
    public ResponseEntity<FicheCompte> retirerRole(@AuthenticationPrincipal UUID auteur, @PathVariable UUID accountId,
                                                   @PathVariable String role, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.retirerRole(auteur, accountId, role, r.motif()));
    }

    @GetMapping("/roles")
    public ResponseEntity<RolesEtPermissions> roles() {
        return ResponseEntity.ok(service.rolesEtPermissions());
    }

    @PostMapping("/roles/{role}/permissions/{permission}")
    public ResponseEntity<RolesEtPermissions> accorder(@AuthenticationPrincipal UUID auteur, @PathVariable String role,
                                                       @PathVariable String permission, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.modifierPermission(auteur, role, permission, true, r.motif()));
    }

    @PostMapping("/roles/{role}/permissions/{permission}/retirer")
    public ResponseEntity<RolesEtPermissions> retirer(@AuthenticationPrincipal UUID auteur, @PathVariable String role,
                                                      @PathVariable String permission, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.modifierPermission(auteur, role, permission, false, r.motif()));
    }
}
