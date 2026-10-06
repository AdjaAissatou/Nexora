package sn.ucad.nexora.catalogue.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Flux;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Profil;
import sn.ucad.nexora.catalogue.application.dto.response.DecouverteDtos.Signal;
import sn.ucad.nexora.catalogue.application.service.DecouverteService;
import sn.ucad.nexora.catalogue.application.service.RechercherOffresService;

/**
 * Nexora Découvrir (§18) : flux vertical d'offres et de professionnels d'une zone, appris des
 * signaux du visiteur. Public : un visiteur est un identifiant anonyme de navigateur, ou
 * « c-&lt;compte&gt; » une fois connecté (fourni par l'application web).
 */
@RestController
@RequestMapping("/api/v1/decouvrir")
@Tag(name = "Catalogue - Découvrir", description = "Flux de découverte local et personnalisé")
public class DecouverteController {

    private static final int MAX_RECHERCHE = 300;

    private final DecouverteService decouverte;
    private final RechercherOffresService recherche;

    public DecouverteController(DecouverteService decouverte, RechercherOffresService recherche) {
        this.decouverte = decouverte;
        this.recherche = recherche;
    }

    @GetMapping
    @Operation(summary = "Page suivante du flux (8 cartes par défaut) ; avec des critères de recherche, le flux ne montre que leurs résultats")
    public ResponseEntity<Flux> flux(
            @RequestParam(required = false) String visiteur,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) BigDecimal lat,
            @RequestParam(required = false) BigDecimal lng,
            @RequestParam(defaultValue = "3") double rayonKm,
            @RequestParam(required = false) List<Long> vus,
            @RequestParam(required = false) List<Long> vusEspaces,
            @RequestParam(defaultValue = "8") int taille,
            @ModelAttribute OffreSearchRequest criteres) {
        List<Long> rechercheClassee = null;
        if (rechercheActive(criteres)) {
            // La zone du flux s'applique à part : pas de second filtre de position dans la recherche
            criteres.setLat(null);
            criteres.setLng(null);
            criteres.setRayonKm(null);
            criteres.setTri("PERTINENCE");
            rechercheClassee = recherche.idsRecherche(criteres, MAX_RECHERCHE);
        }
        return ResponseEntity.ok(decouverte.flux(visiteur, zone, lat, lng, Math.max(0.2, Math.min(rayonKm, 50)),
                rechercheClassee, vus == null ? List.of() : vus, vusEspaces == null ? List.of() : vusEspaces,
                Math.max(3, Math.min(taille, 20))));
    }

    private static boolean rechercheActive(OffreSearchRequest c) {
        return (c.getQ() != null && !c.getQ().isBlank()) || c.getIdCategorie() != null
                || (c.getTypeEspace() != null && !c.getTypeEspace().isBlank()) || (c.getCommune() != null && !c.getCommune().isBlank())
                || c.getPrixMin() != null || c.getPrixMax() != null || c.getEstProduit() != null
                || Boolean.TRUE.equals(c.getAvecPromotion()) || Boolean.TRUE.equals(c.getEspaceVerifie())
                || Boolean.TRUE.equals(c.getOuvertMaintenant()) || c.getNoteMin() != null || c.getNeuf() != null
                || Boolean.TRUE.equals(c.getNegociable()) || Boolean.TRUE.equals(c.getDomicile())
                || (c.getValeurs() != null && !c.getValeurs().isEmpty());
    }

    @PostMapping("/signaux")
    @Operation(summary = "Enregistrer une interaction (vue, j'aime, enregistré, contact…)")
    public ResponseEntity<Void> signal(@RequestBody Signal signal) {
        try {
            decouverte.signal(signal);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/signaux/j-aime")
    @Operation(summary = "Retirer un « J'aime »")
    public ResponseEntity<Void> retirerJAime(@RequestParam String visiteur, @RequestParam(required = false) Long idOffre,
                                             @RequestParam(required = false) Long idEspace) {
        decouverte.retirerJAime(visiteur, idOffre, idEspace);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profil")
    @Operation(summary = "Ce que Nexora a compris des goûts du visiteur")
    public ResponseEntity<Profil> profil(@RequestParam String visiteur) {
        return ResponseEntity.ok(decouverte.profil(visiteur));
    }

    @DeleteMapping("/profil")
    @Operation(summary = "Oublier les goûts du visiteur (efface ses signaux)")
    public ResponseEntity<Map<String, Integer>> oublier(@RequestParam String visiteur) {
        return ResponseEntity.ok(Map.of("signauxEffaces", decouverte.oublier(visiteur)));
    }

    @PostMapping("/fusion")
    @Operation(summary = "À la connexion : rattacher l'historique anonyme du navigateur au compte")
    public ResponseEntity<Map<String, Integer>> fusionner(@RequestParam String ancien, @RequestParam String nouveau) {
        return ResponseEntity.ok(Map.of("signauxRattaches", decouverte.fusionner(ancien, nouveau)));
    }
}
