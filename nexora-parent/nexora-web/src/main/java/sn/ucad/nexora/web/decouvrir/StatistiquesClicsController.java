package sn.ucad.nexora.web.decouvrir;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import sn.ucad.nexora.web.config.GatewayConfig;

/**
 * Clics sur Appeler, WhatsApp, Itinéraire et Partager depuis les fiches (docs/architecture-acteurs.md §26),
 * relayés au catalogue avec l'identifiant du visiteur (le même que pour les « J'aime »). Toujours 204 :
 * un clic perdu n'a pas d'effet pour le visiteur.
 */
@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesClicsController {

    private static final Set<String> TYPES = Set.of("APPEL", "WHATSAPP", "ITINERAIRE", "PARTAGE");
    private static final RestClient CATALOGUE = RestClient.builder()
            .baseUrl(GatewayConfig.gatewayUrl() + "/catalogue-service/api/v1/statistiques").build();

    @PostMapping("/clic")
    public ResponseEntity<Void> clic(@RequestBody Map<String, Object> clic, HttpServletRequest requete, HttpServletResponse reponse) {
        Object type = clic.get("type");
        if (type instanceof String t && TYPES.contains(t)) {
            Map<String, Object> corps = new LinkedHashMap<>();
            corps.put("type", t);
            corps.put("idOffre", nombre(clic.get("idOffre")));
            corps.put("idEspace", nombre(clic.get("idEspace")));
            corps.put("visiteur", Reactions.visiteur(requete, reponse));
            try {
                CATALOGUE.post().uri("/evenements").contentType(MediaType.APPLICATION_JSON).body(corps).retrieve().toBodilessEntity();
            } catch (Exception e) {
                // Statistique perdue : sans conséquence pour le visiteur
            }
        }
        return ResponseEntity.noContent().build();
    }

    private static Long nombre(Object o) {
        if (o instanceof Number n) return n.longValue();
        if (o instanceof String s && s.matches("\\d+")) return Long.valueOf(s);
        return null;
    }
}
