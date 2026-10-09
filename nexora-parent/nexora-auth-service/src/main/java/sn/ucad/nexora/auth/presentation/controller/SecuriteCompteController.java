package sn.ucad.nexora.auth.presentation.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.ucad.nexora.auth.application.dto.securite.SecuriteDtos.*;
import sn.ucad.nexora.auth.application.port.outbound.JwtProviderPort;
import sn.ucad.nexora.auth.application.service.SecuriteCompteService;

/** Sécurité du compte connecté (docs/architecture-acteurs.md §25). */
@RestController
@RequestMapping("/api/v1/compte/securite")
public class SecuriteCompteController {

    private final SecuriteCompteService service;
    private final JwtProviderPort jwt;

    public SecuriteCompteController(SecuriteCompteService service, JwtProviderPort jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @GetMapping
    public ResponseEntity<Securite> consulter(@AuthenticationPrincipal UUID compte, HttpServletRequest requete) {
        return ResponseEntity.ok(service.consulter(compte, sid(requete)));
    }

    @PostMapping("/mot-de-passe")
    public ResponseEntity<Resultat> motDePasse(@AuthenticationPrincipal UUID compte, @RequestBody ChangementMotDePasse r,
                                               HttpServletRequest requete) {
        return ResponseEntity.ok(service.changerMotDePasse(compte, sid(requete), r));
    }

    @PostMapping("/email")
    public ResponseEntity<Resultat> email(@AuthenticationPrincipal UUID compte, @RequestBody DemandeChangementEmail r) {
        return ResponseEntity.ok(service.demanderEmail(compte, r));
    }

    /** Réponse : {@code message} = la nouvelle adresse email. */
    @PostMapping("/email/confirmer")
    public ResponseEntity<Resultat> confirmer(@AuthenticationPrincipal UUID compte, @RequestBody ConfirmationEmail r,
                                              HttpServletRequest requete) {
        return ResponseEntity.ok(service.confirmerEmail(compte, sid(requete), r));
    }

    @PostMapping("/sessions/{sid}/terminer")
    public ResponseEntity<Resultat> terminer(@AuthenticationPrincipal UUID compte, @PathVariable String sid,
                                             HttpServletRequest requete) {
        return ResponseEntity.ok(service.terminerSession(compte, sid, sid(requete)));
    }

    @PostMapping("/sessions/terminer-autres")
    public ResponseEntity<Resultat> terminerAutres(@AuthenticationPrincipal UUID compte, HttpServletRequest requete) {
        return ResponseEntity.ok(service.terminerAutres(compte, sid(requete)));
    }

    /** Session de la requête : claim « sid » du jeton d'accès (null pour un jeton d'avant §25). */
    private String sid(HttpServletRequest requete) {
        String h = requete.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) return null;
        try {
            Claims c = jwt.parseToken(h.substring(7));
            return c.get("sid", String.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
