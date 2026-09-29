package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.user.UpdateUserRequest;
import sn.ucad.nexora.web.dto.user.UserResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de user-service (via l'API Gateway) : profil utilisateur.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class UserApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(UserApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/user-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl).build();

    public UserResponse obtenir(String accessToken, UUID accountId) {
        LOG.info("Requête GET {}/api/v1/users/{}", baseUrl, accountId);
        try {
            return client
                    .get()
                    .uri("/api/v1/users/{accountId}", accountId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(UserResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public UserResponse mettreAJour(String accessToken, UpdateUserRequest requete) {
        LOG.info("Requête PUT {}/api/v1/users/me", baseUrl);
        try {
            return client
                    .put()
                    .uri("/api/v1/users/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requete)
                    .retrieve()
                    .body(UserResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
