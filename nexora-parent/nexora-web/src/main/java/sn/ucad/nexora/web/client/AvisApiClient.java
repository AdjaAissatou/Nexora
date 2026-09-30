package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.recherche.AvisResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de recherche-service (via l'API Gateway) : avis publiés sur les offres et espaces, et
 * signalements envoyés par les utilisateurs (§9.10).
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class AvisApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(AvisApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/recherche-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl)
            .requestInterceptor(sn.ucad.nexora.web.config.EnTetesClient.IP_NAVIGATEUR).build();

    public List<AvisResponse> parEspace(Long espaceId) {
        LOG.info("Requête GET {}/api/v1/avis/espace/{}", baseUrl, espaceId);
        try {
            return client.get().uri("/api/v1/avis/espace/{id}", espaceId).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<AvisResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    /** Mon avis sur un espace, même masqué par la modération ; null si je n'en ai pas. */
    public record MonAvis(Long id, int note, String commentaire, boolean masque, String motifModeration) implements java.io.Serializable {}

    public MonAvis mien(String accessToken, Long espaceId) {
        return appel(() -> client.get().uri(u -> u.path("/api/v1/avis/mien").queryParam("espaceId", espaceId).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(MonAvis.class));
    }

    public AvisResponse un(Long avisId) {
        return appel(() -> client.get().uri("/api/v1/avis/{id}", avisId).retrieve().body(AvisResponse.class));
    }

    public AvisResponse publier(String accessToken, Long espaceId, int note, String commentaire) {
        LOG.info("Requête POST {}/api/v1/avis (espace {})", baseUrl, espaceId);
        return appel(() -> client.post().uri("/api/v1/avis")
                .header("Authorization", "Bearer " + accessToken)
                .body(java.util.Map.of("espaceId", espaceId, "note", note, "commentaire", commentaire == null ? "" : commentaire))
                .retrieve().body(AvisResponse.class));
    }

    /** Motifs de signalement proposés : code → libellé. */
    public java.util.Map<String, String> motifsSignalement() {
        return appel(() -> client.get().uri("/api/v1/signalements/motifs").retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, String>>() {}));
    }

    public void signaler(String accessToken, Long espaceId, Long offreId, Long avisId, String motif, String description) {
        LOG.info("Requête POST {}/api/v1/signalements", baseUrl);
        java.util.Map<String, Object> corps = new java.util.HashMap<>();
        corps.put("espaceId", espaceId);
        corps.put("offreId", offreId);
        corps.put("avisId", avisId);
        corps.put("motif", motif);
        corps.put("description", description);
        appel(() -> client.post().uri("/api/v1/signalements")
                .header("Authorization", "Bearer " + accessToken)
                .body(corps).retrieve().toBodilessEntity());
    }

    private static <T> T appel(java.util.function.Supplier<T> requete) {
        try {
            return requete.get();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (sn.ucad.nexora.web.error.ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
