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
 * Client de recherche-service (via l'API Gateway) : avis publiés sur les offres et espaces.
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
}
