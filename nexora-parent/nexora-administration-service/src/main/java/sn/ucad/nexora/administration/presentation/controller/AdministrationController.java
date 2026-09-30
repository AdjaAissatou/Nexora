package sn.ucad.nexora.administration.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.PageJournalResponse;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.TableauDeBordResponse;
import sn.ucad.nexora.administration.application.service.AdministrationService;

/** Accès contrôlé par rôle dans SecurityConfig (§9.2). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdministrationController {

    private final AdministrationService service;

    public AdministrationController(AdministrationService service) {
        this.service = service;
    }

    /** Tout le back-office ; les dernières actions du journal seulement avec la permission GERER_JOURNAL. */
    @GetMapping("/tableau-de-bord")
    public ResponseEntity<TableauDeBordResponse> tableauDeBord(Authentication auth) {
        boolean journal = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PERM_GERER_JOURNAL"));
        return ResponseEntity.ok(service.tableauDeBord(journal));
    }

    @GetMapping("/journal")
    public ResponseEntity<PageJournalResponse> journal(@RequestParam(required = false) String module,
                                                       @RequestParam(required = false) String recherche,
                                                       @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(service.journal(module, recherche, page));
    }
}
