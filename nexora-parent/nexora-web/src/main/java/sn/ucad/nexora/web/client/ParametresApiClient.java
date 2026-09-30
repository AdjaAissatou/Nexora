package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.EnTetesClient;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.Parametre;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.VueJustificatifs;
import sn.ucad.nexora.web.error.ApiErrors;
import sn.ucad.nexora.web.error.ApiException;

/**
 * Paramètres de Nexora (administration-service) et règles des justificatifs de vérification
 * (espace-service), via l'API Gateway, §9.12.
 */
@ApplicationScoped
public class ParametresApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(ParametresApiClient.class);
    private static final String JUSTIFICATIFS = "/api/v1/admin/justificatifs";
    private final RestClient administration = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/administration-service")
            .requestInterceptor(EnTetesClient.IP_NAVIGATEUR).build();
    private final RestClient espace = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/espace-service")
            .requestInterceptor(EnTetesClient.IP_NAVIGATEUR).build();

    /** Paramètres publics (code → valeur), sans jeton ; lève une exception si le service ne répond pas. */
    public Map<String, String> publics() {
        return administration.get().uri("/api/v1/public/parametres").retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {});
    }

    public List<Parametre> parametres(String jeton) {
        return appel(() -> administration.get().uri("/api/v1/admin/parametres").header("Authorization", "Bearer " + jeton)
                .retrieve().body(new ParameterizedTypeReference<List<Parametre>>() {}));
    }

    public Parametre modifier(String jeton, String code, String valeur, String motif) {
        LOG.info("Requête PUT /api/v1/admin/parametres/{}", code);
        return appel(() -> administration.put().uri("/api/v1/admin/parametres/{code}", code).header("Authorization", "Bearer " + jeton)
                .body(Map.of("valeur", valeur == null ? "" : valeur, "motif", motif == null ? "" : motif))
                .retrieve().body(Parametre.class));
    }

    public VueJustificatifs justificatifs(String jeton) {
        return appel(() -> espace.get().uri(JUSTIFICATIFS).header("Authorization", "Bearer " + jeton)
                .retrieve().body(VueJustificatifs.class));
    }

    /** POST ou PUT sur les règles des justificatifs, renvoyant la vue à jour. */
    public VueJustificatifs envoyer(String jeton, String methode, String chemin, Object corps, Object... variables) {
        LOG.info("Requête {} {}{}", methode, JUSTIFICATIFS, chemin);
        return appel(() -> ("PUT".equals(methode) ? espace.put() : espace.post()).uri(JUSTIFICATIFS + chemin, variables)
                .header("Authorization", "Bearer " + jeton).body(corps).retrieve().body(VueJustificatifs.class));
    }

    private static <T> T appel(java.util.function.Supplier<T> requete) {
        try {
            return requete.get();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
