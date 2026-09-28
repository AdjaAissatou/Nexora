package sn.ucad.nexora.espace.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.ucad.nexora.espace.application.dto.response.CommuneResponse;
import sn.ucad.nexora.espace.application.dto.response.DepartementResponse;
import sn.ucad.nexora.espace.application.dto.response.RegionResponse;
import sn.ucad.nexora.espace.infrastructure.persistence.GeoQueryRepository;

/**
 * Référence géographique du Sénégal (région > département > commune) — public, en lecture.
 * Alimente la sélection en cascade de la localisation dans les formulaires "créer/modifier un espace".
 */
@RestController
@RequestMapping("/api/v1/geo")
public class GeoController {

    private final GeoQueryRepository geo;

    public GeoController(GeoQueryRepository geo) {
        this.geo = geo;
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> regions() {
        return ResponseEntity.ok(geo.regions());
    }

    @GetMapping("/regions/{id}/departements")
    public ResponseEntity<List<DepartementResponse>> departements(@PathVariable Long id) {
        return ResponseEntity.ok(geo.departements(id));
    }

    @GetMapping("/departements/{id}/communes")
    public ResponseEntity<List<CommuneResponse>> communes(@PathVariable Long id) {
        return ResponseEntity.ok(geo.communes(id));
    }
}
