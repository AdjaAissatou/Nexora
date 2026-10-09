package sn.ucad.nexora.web.decouvrir;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.inject.spi.CDI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.client.RechercheApiClient;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Nexora Découvrir (§18), côté navigateur : la page {@code decouvrir.xhtml} charge le flux et
 * envoie ses signaux ici (même origine), et ce contrôleur les relaie à catalogue-service en y
 * ajoutant l'identité du visiteur — un identifiant anonyme gardé dans un cookie, ou le compte
 * une fois connecté (le profil suit alors la personne d'un appareil à l'autre ; l'historique
 * anonyme du navigateur y est rattaché à la première visite connectée).
 */
@RestController
@RequestMapping("/api/decouvrir")
public class DecouvrirController {

    private static final Logger LOG = LoggerFactory.getLogger(DecouvrirController.class);
    /** Paramètres du flux relayés tels quels (zone, position, déjà vus, critères d'une recherche). */
    private static final Set<String> PARAMETRES = Set.of("zone", "lat", "lng", "rayonKm", "vus", "vusEspaces", "taille",
            "q", "idCategorie", "typeEspace", "commune", "prixMin", "prixMax", "estProduit", "avecPromotion",
            "espaceVerifie", "ouvertMaintenant", "noteMin", "neuf", "negociable", "domicile", "valeurs", "nature");

    private final RestClient catalogue = RestClient.builder()
            .baseUrl(GatewayConfig.gatewayUrl() + "/catalogue-service/api/v1/decouvrir").build();

    private static SessionBean session() {
        return Reactions.session();
    }

    @GetMapping(value = "/flux", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> flux(@RequestParam MultiValueMap<String, String> parametres,
                                       HttpServletRequest requete, HttpServletResponse reponse) {
        String visiteur = visiteur(requete, reponse);
        try {
            String corps = catalogue.get().uri(u -> {
                u.queryParam("visiteur", visiteur);
                parametres.forEach((cle, valeurs) -> {
                    if (PARAMETRES.contains(cle)) {
                        valeurs.stream().filter(v -> v != null && !v.isBlank()).forEach(v -> u.queryParam(cle, v));
                    }
                });
                return u.build();
            }).retrieve().body(String.class);
            return ResponseEntity.ok(corps);
        } catch (RestClientResponseException e) {
            LOG.warn("Flux Découvrir indisponible : {}", e.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"cartes\":[],\"fin\":true,\"erreur\":true}");
        } catch (Exception e) {
            LOG.warn("Flux Découvrir indisponible : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"cartes\":[],\"fin\":true,\"erreur\":true}");
        }
    }

    /** Un signal du navigateur : {idOffre?, idEspace?, type, dureeMs?}. */
    @PostMapping("/signal")
    public ResponseEntity<Void> signal(@RequestBody Map<String, Object> signal, HttpServletRequest requete, HttpServletResponse reponse) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("visiteur", visiteur(requete, reponse));
        corps.put("idOffre", signal.get("idOffre"));
        corps.put("idEspace", signal.get("idEspace"));
        corps.put("type", signal.get("type"));
        corps.put("dureeMs", signal.get("dureeMs"));
        try {
            catalogue.post().uri("/signaux").contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().toBodilessEntity();
            return ResponseEntity.noContent().build();
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @DeleteMapping("/jaime")
    public ResponseEntity<Void> retirerJAime(@RequestParam(required = false) Long idOffre, @RequestParam(required = false) Long idEspace,
                                             HttpServletRequest requete, HttpServletResponse reponse) {
        String visiteur = visiteur(requete, reponse);
        try {
            catalogue.delete().uri(u -> u.path("/signaux/j-aime").queryParam("visiteur", visiteur)
                    .queryParamIfPresent("idOffre", java.util.Optional.ofNullable(idOffre))
                    .queryParamIfPresent("idEspace", java.util.Optional.ofNullable(idEspace)).build()).retrieve().toBodilessEntity();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /** « Enregistrer » = favori du compte ; sans compte, la page propose de se connecter. */
    @PostMapping("/enregistrer")
    public ResponseEntity<Map<String, Object>> enregistrer(@RequestBody Map<String, Object> cible, HttpServletRequest requete,
                                                           HttpServletResponse reponse) {
        SessionBean s = session();
        if (s == null || !s.isConnecte()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("connexion", "/connexion.xhtml?redirect=/decouvrir.xhtml"));
        }
        Long idOffre = nombre(cible.get("idOffre"));
        Long idEspace = nombre(cible.get("idEspace"));
        try {
            CDI.current().select(RechercheApiClient.class).get().ajouterFavori(s.getAccessToken(), idOffre, idEspace);
        } catch (ApiException e) {
            // Déjà en favori : rien à faire, l'enregistrement est acquis
            LOG.info("Favori non ajouté : {}", e.getMessage());
        }
        Map<String, Object> enregistre = new LinkedHashMap<>();
        enregistre.put("type", "ENREGISTRE");
        enregistre.put("idOffre", idOffre);
        enregistre.put("idEspace", idEspace);
        signal(enregistre, requete, reponse);
        return ResponseEntity.ok(Map.of("enregistre", true));
    }

    /** Quartiers ayant des offres visibles, pour choisir la zone du flux. */
    @GetMapping(value = "/zones", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> zones() {
        try {
            Map<?, ?> explorer = RestClient.create(GatewayConfig.gatewayUrl() + "/catalogue-service/api/v1/explorer")
                    .get().retrieve().body(Map.class);
            return ResponseEntity.ok(explorer == null ? java.util.List.of() : explorer.get("quartiers"));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.List.of());
        }
    }

    @GetMapping(value = "/gouts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> gouts(HttpServletRequest requete, HttpServletResponse reponse) {
        String visiteur = visiteur(requete, reponse);
        try {
            return ResponseEntity.ok(catalogue.get().uri(u -> u.path("/profil").queryParam("visiteur", visiteur).build())
                    .retrieve().body(String.class));
        } catch (Exception e) {
            return ResponseEntity.ok("{\"interets\":[],\"nombreSignaux\":0}");
        }
    }

    @DeleteMapping("/gouts")
    public ResponseEntity<Void> oublier(HttpServletRequest requete, HttpServletResponse reponse) {
        String visiteur = visiteur(requete, reponse);
        try {
            catalogue.delete().uri(u -> u.path("/profil").queryParam("visiteur", visiteur).build()).retrieve().toBodilessEntity();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /** Le visiteur : voir {@link Reactions#visiteur}. */
    private String visiteur(HttpServletRequest requete, HttpServletResponse reponse) {
        return Reactions.visiteur(requete, reponse);
    }

    private static Long nombre(Object o) {
        if (o instanceof Number n) return n.longValue();
        if (o instanceof String t && t.matches("\\d+")) return Long.valueOf(t);
        return null;
    }
}
