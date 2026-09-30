package sn.ucad.nexora.catalogue.presentation.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.catalogue.application.dto.response.admin.CatalogueAdminDtos.*;
import sn.ucad.nexora.catalogue.application.service.admin.CatalogueAdminService;

/**
 * Gestion du catalogue (§9.11). Permissions dans SecurityConfig : lecture avec l'une des trois ;
 * catégories et types d'espace : GERER_CATEGORIES ; types d'offre : GERER_TYPES_OFFRES ;
 * attributs et valeurs : GERER_ATTRIBUTS.
 */
@RestController
@RequestMapping("/api/v1/admin/catalogue")
public class CatalogueAdminController {

    private final CatalogueAdminService service;

    public CatalogueAdminController(CatalogueAdminService service) {
        this.service = service;
    }

    // ------------------------------------------------------------------ lecture

    @GetMapping("/categories")
    public ResponseEntity<List<Noeud>> racines() {
        return ResponseEntity.ok(service.racines());
    }

    @GetMapping("/categories/{id:\\d+}")
    public ResponseEntity<FicheCategorie> fiche(@PathVariable Long id) {
        return ResponseEntity.ok(service.fiche(id));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<Resultat>> rechercher(@RequestParam String q) {
        return ResponseEntity.ok(service.rechercher(q));
    }

    // ------------------------------------------------------------------ catégories

    @PostMapping("/categories")
    public ResponseEntity<FicheCategorie> creerCategorie(@AuthenticationPrincipal UUID c, @RequestBody CategorieRequest r) {
        return ResponseEntity.ok(service.creerCategorie(c, r));
    }

    @PutMapping("/categories/{id:\\d+}")
    public ResponseEntity<FicheCategorie> modifierCategorie(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody CategorieRequest r) {
        return ResponseEntity.ok(service.modifierCategorie(c, id, r));
    }

    @PostMapping("/categories/{id:\\d+}/activer")
    public ResponseEntity<FicheCategorie> activerCategorie(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.activerCategorie(c, id, true, r.motif()));
    }

    @PostMapping("/categories/{id:\\d+}/desactiver")
    public ResponseEntity<FicheCategorie> desactiverCategorie(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(service.activerCategorie(c, id, false, r.motif()));
    }

    /** Réponse : la fiche du parent, ou 204 pour une catégorie racine. */
    @PostMapping("/categories/{id:\\d+}/supprimer")
    public ResponseEntity<FicheCategorie> supprimerCategorie(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody MotifRequest r) {
        FicheCategorie parent = service.supprimerCategorie(c, id, r.motif());
        return parent == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(parent);
    }

    @PostMapping("/categories/{id:\\d+}/types-espace/{te:\\d+}")
    public ResponseEntity<FicheCategorie> lier(@AuthenticationPrincipal UUID c, @PathVariable Long id, @PathVariable Long te) {
        return ResponseEntity.ok(service.lierTypeEspace(c, id, te, true));
    }

    @PostMapping("/categories/{id:\\d+}/types-espace/{te:\\d+}/retirer")
    public ResponseEntity<FicheCategorie> delier(@AuthenticationPrincipal UUID c, @PathVariable Long id, @PathVariable Long te) {
        return ResponseEntity.ok(service.lierTypeEspace(c, id, te, false));
    }

    // ------------------------------------------------------------------ types d'offre

    @PostMapping("/categories/{id:\\d+}/types")
    public ResponseEntity<FicheCategorie> creerType(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody TypeOffreRequest r) {
        return ResponseEntity.ok(service.creerType(c, id, r));
    }

    @PutMapping("/types/{id:\\d+}")
    public ResponseEntity<FicheCategorie> modifierType(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody TypeOffreRequest r) {
        return ResponseEntity.ok(service.modifierType(c, id, r));
    }

    @PostMapping("/types/{id:\\d+}/{action:activer|desactiver|supprimer}")
    public ResponseEntity<FicheCategorie> actionType(@AuthenticationPrincipal UUID c, @PathVariable Long id, @PathVariable String action,
                                                     @RequestBody MotifRequest r) {
        return ResponseEntity.ok("supprimer".equals(action) ? service.supprimerType(c, id, r.motif())
                : service.activerType(c, id, "activer".equals(action), r.motif()));
    }

    // ------------------------------------------------------------------ attributs et valeurs

    @PostMapping("/categories/{id:\\d+}/attributs")
    public ResponseEntity<FicheCategorie> creerAttribut(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody AttributRequest r) {
        return ResponseEntity.ok(service.creerAttribut(c, id, r));
    }

    @PutMapping("/attributs/{id:\\d+}")
    public ResponseEntity<FicheCategorie> modifierAttribut(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody AttributRequest r) {
        return ResponseEntity.ok(service.modifierAttribut(c, id, r));
    }

    @PostMapping("/attributs/{id:\\d+}/{action:activer|desactiver|supprimer}")
    public ResponseEntity<FicheCategorie> actionAttribut(@AuthenticationPrincipal UUID c, @PathVariable Long id, @PathVariable String action,
                                                         @RequestBody MotifRequest r) {
        return ResponseEntity.ok("supprimer".equals(action) ? service.supprimerAttribut(c, id, r.motif())
                : service.activerAttribut(c, id, "activer".equals(action), r.motif()));
    }

    @PostMapping("/attributs/{id:\\d+}/valeurs")
    public ResponseEntity<FicheCategorie> creerValeur(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody ValeurRequest r) {
        return ResponseEntity.ok(service.creerValeur(c, id, r));
    }

    @PutMapping("/valeurs/{id:\\d+}")
    public ResponseEntity<FicheCategorie> modifierValeur(@AuthenticationPrincipal UUID c, @PathVariable Long id, @RequestBody ValeurRequest r) {
        return ResponseEntity.ok(service.modifierValeur(c, id, r));
    }

    @PostMapping("/valeurs/{id:\\d+}/{action:activer|desactiver|supprimer}")
    public ResponseEntity<FicheCategorie> actionValeur(@AuthenticationPrincipal UUID c, @PathVariable Long id, @PathVariable String action,
                                                       @RequestBody MotifRequest r) {
        return ResponseEntity.ok("supprimer".equals(action) ? service.supprimerValeur(c, id, r.motif())
                : service.activerValeur(c, id, "activer".equals(action), r.motif()));
    }
}
