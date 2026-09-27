package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
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

    private final RestClient client = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/auth-service").build();

    public AuthenticationResponse connecter(String email, String motDePasse) {
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
        try {
            return client.post().uri("/api/v1/auth/register").body(requete).retrieve().body(RegisterResult.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
