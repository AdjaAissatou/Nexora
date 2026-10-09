package sn.ucad.nexora.catalogue.presentation.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.response.StatistiquesDtos.Evenement;
import sn.ucad.nexora.catalogue.application.dto.response.StatistiquesDtos.StatistiquesEspace;
import sn.ucad.nexora.catalogue.application.service.statistiques.StatistiquesService;

/** Statistiques détaillées d'un espace (docs/architecture-acteurs.md §26). */
@RestController
@RequestMapping("/api/v1/statistiques")
public class StatistiquesController {

    private final StatistiquesService service;

    public StatistiquesController(StatistiquesService service) {
        this.service = service;
    }

    /** Propriétaire de l'espace seulement. */
    @GetMapping("/espaces/{id:\\d+}")
    public ResponseEntity<StatistiquesEspace> espace(@AuthenticationPrincipal UUID compte, @PathVariable Long id,
                                                     @RequestParam(defaultValue = "12") int semaines) {
        return ResponseEntity.ok(service.espace(compte, id, semaines));
    }

    /** Clic sur Appeler, WhatsApp, Itinéraire ou Partager (public, relayé par le web avec l'identifiant du visiteur). */
    @PostMapping("/evenements")
    public ResponseEntity<Void> evenement(@RequestBody Evenement e) {
        service.enregistrer(e);
        return ResponseEntity.noContent().build();
    }
}
