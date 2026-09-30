package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.auth.AuthenticationResponse;
import sn.ucad.nexora.web.dto.auth.ForgotPasswordRequest;
import sn.ucad.nexora.web.dto.auth.LoginRequest;
import sn.ucad.nexora.web.dto.auth.OtpResponse;
import sn.ucad.nexora.web.dto.auth.RefreshTokenRequest;
import sn.ucad.nexora.web.dto.auth.RegisterRequest;
import sn.ucad.nexora.web.dto.auth.RegisterResult;
import sn.ucad.nexora.web.dto.auth.ResetPasswordRequest;
import sn.ucad.nexora.web.dto.auth.VerifyOtpRequest;
import sn.ucad.nexora.web.dto.auth.VerifyOtpResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de auth-service (via l'API Gateway) : inscription, connexion.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class AuthApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuthApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/auth-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl)
            .requestInterceptor(sn.ucad.nexora.web.config.EnTetesClient.IP_NAVIGATEUR).build();

    public AuthenticationResponse connecter(String email, String motDePasse) {
        LOG.info("Requête POST {}/api/v1/auth/login", baseUrl);
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

    /** Nouveau jeton d'accès, régénéré par auth-service à partir des rôles actuels du compte en base. */
    public AuthenticationResponse rafraichir(String refreshToken) {
        LOG.info("Requête POST {}/api/v1/auth/refresh", baseUrl);
        try {
            return client
                    .post()
                    .uri("/api/v1/auth/refresh")
                    .body(new RefreshTokenRequest(refreshToken))
                    .retrieve()
                    .body(AuthenticationResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public RegisterResult inscrire(RegisterRequest requete) {
        LOG.info("Requête POST {}/api/v1/auth/register", baseUrl);
        try {
            return client.post().uri("/api/v1/auth/register").body(requete).retrieve().body(RegisterResult.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public VerifyOtpResponse verifierOtp(String email, String otp) {
        LOG.info("Requête POST {}/api/v1/auth/verify-otp", baseUrl);
        try {
            return client
                    .post()
                    .uri("/api/v1/auth/verify-otp")
                    .body(new VerifyOtpRequest(email, otp))
                    .retrieve()
                    .body(VerifyOtpResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public OtpResponse motDePasseOublie(String email) {
        LOG.info("Requête POST {}/api/v1/auth/forgot-password", baseUrl);
        try {
            return client
                    .post()
                    .uri("/api/v1/auth/forgot-password")
                    .body(new ForgotPasswordRequest(email))
                    .retrieve()
                    .body(OtpResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public OtpResponse reinitialiserMotDePasse(String email, String otp, String motDePasse, String confirmation) {
        LOG.info("Requête POST {}/api/v1/auth/reset-password", baseUrl);
        try {
            return client
                    .post()
                    .uri("/api/v1/auth/reset-password")
                    .body(new ResetPasswordRequest(email, otp, motDePasse, confirmation))
                    .retrieve()
                    .body(OtpResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
