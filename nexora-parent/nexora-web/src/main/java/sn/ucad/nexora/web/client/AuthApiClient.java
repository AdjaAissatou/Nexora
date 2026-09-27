package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.auth.AuthenticationResponse;
import sn.ucad.nexora.web.dto.auth.LoginRequest;
import sn.ucad.nexora.web.dto.auth.RegisterRequest;
import sn.ucad.nexora.web.dto.auth.RegisterResult;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de auth-service (via l'API Gateway) : inscription, connexion.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class AuthApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuthApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/auth-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl).build();

    public AuthenticationResponse connecter(String email, String motDePasse) {
        LOG.info("POST {}/api/v1/auth/login", baseUrl);
        try {
            return client
                    .post()
                    .uri("/api/v1/auth/login")
                    .body(new LoginRequest(email, motDePasse))
                    .retrieve()
                    .body(AuthenticationResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public RegisterResult inscrire(RegisterRequest requete) {
        LOG.info("POST {}/api/v1/auth/register", baseUrl);
        try {
            return client.post().uri("/api/v1/auth/register").body(requete).retrieve().body(RegisterResult.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
