package sn.ucad.nexora.recherche.presentation.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.AvisModere;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.PageAvis;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.PageSignalements;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.SignalementDetail;
import sn.ucad.nexora.recherche.application.service.moderation.AvisModerationService;
import sn.ucad.nexora.recherche.application.service.moderation.SignalementAdminService;

/**
 * Modération des avis (permission MODERER_AVIS) et file des signalements (GERER_SIGNALEMENTS),
 * permissions exigées par SecurityConfig (docs/architecture-acteurs.md §9.10).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class ModerationRechercheController {

    /** Motif d'une décision, ou commentaire de clôture d'un signalement. */
    public record MotifRequest(String motif) {}

    private final AvisModerationService avis;
    private final SignalementAdminService signalements;

    public ModerationRechercheController(AvisModerationService avis, SignalementAdminService signalements) {
        this.avis = avis;
        this.signalements = signalements;
    }

    // ------------------------------------------------------------------ avis

    @GetMapping("/avis")
    public ResponseEntity<PageAvis> avis(@RequestParam(required = false) String recherche,
                                         @RequestParam(required = false) String etat,
                                         @RequestParam(required = false) Long idEspace,
                                         @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(avis.rechercher(recherche, etat, idEspace, page));
    }

    @PostMapping("/avis/{id:\\d+}/masquer")
    public ResponseEntity<AvisModere> masquer(@AuthenticationPrincipal UUID compte, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(avis.masquer(compte, id, r.motif()));
    }

    @PostMapping("/avis/{id:\\d+}/retablir")
    public ResponseEntity<AvisModere> retablir(@AuthenticationPrincipal UUID compte, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(avis.retablir(compte, id, r.motif()));
    }

    // ------------------------------------------------------------------ signalements

    @GetMapping("/signalements")
    public ResponseEntity<PageSignalements> signalements(@RequestParam(required = false) String statut,
                                                         @RequestParam(required = false) String type,
                                                         @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(signalements.rechercher(statut, type, page));
    }

    @GetMapping("/signalements/{id:\\d+}")
    public ResponseEntity<SignalementDetail> signalement(@PathVariable Long id) {
        return ResponseEntity.ok(signalements.detail(id));
    }

    @PostMapping("/signalements/{id:\\d+}/prendre")
    public ResponseEntity<SignalementDetail> prendre(@AuthenticationPrincipal UUID compte, @PathVariable Long id) {
        return ResponseEntity.ok(signalements.prendreEnCharge(compte, id));
    }

    @PostMapping("/signalements/{id:\\d+}/traiter")
    public ResponseEntity<SignalementDetail> traiter(@AuthenticationPrincipal UUID compte, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(signalements.clore(compte, id, true, r.motif()));
    }

    @PostMapping("/signalements/{id:\\d+}/rejeter")
    public ResponseEntity<SignalementDetail> rejeter(@AuthenticationPrincipal UUID compte, @PathVariable Long id, @RequestBody MotifRequest r) {
        return ResponseEntity.ok(signalements.clore(compte, id, false, r.motif()));
    }
}
