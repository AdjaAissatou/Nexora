package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.recherche.AjouterFavoriRequest;
import sn.ucad.nexora.web.dto.recherche.EnregistrerConsultationRequest;
import sn.ucad.nexora.web.dto.recherche.FavoriResponse;
import sn.ucad.nexora.web.dto.recherche.HistoriqueConsultationResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de recherche-service (via l'API Gateway) : favoris et historique de
 * consultation du compte connecté. Bean CDI (et non Spring) pour rester injectable
 * directement dans les managed beans JSF.
 */
@ApplicationScoped
public class RechercheApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(RechercheApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/recherche-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl)
            .requestInterceptor(sn.ucad.nexora.web.config.EnTetesClient.IP_NAVIGATEUR).build();

    public List<FavoriResponse> listerFavoris(String accessToken) {
        LOG.info("Requête GET {}/api/v1/favoris", baseUrl);
        try {
            return client.get().uri("/api/v1/favoris")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<FavoriResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public void ajouterFavori(String accessToken, Long offreId, Long espaceId) {
        LOG.info("Requête POST {}/api/v1/favoris (offreId={}, espaceId={})", baseUrl, offreId, espaceId);
        try {
            client.post().uri("/api/v1/favoris")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(new AjouterFavoriRequest(offreId, espaceId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public void supprimerFavori(String accessToken, Long offreId, Long espaceId) {
        LOG.info("Requête DELETE {}/api/v1/favoris (offreId={}, espaceId={})", baseUrl, offreId, espaceId);
        try {
            client.delete()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/favoris")
                            .queryParamIfPresent("offreId", java.util.Optional.ofNullable(offreId))
                            .queryParamIfPresent("espaceId", java.util.Optional.ofNullable(espaceId))
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public void enregistrerConsultation(String accessToken, Long offreId, Long espaceId) {
        try {
            client.post().uri("/api/v1/historique/consultations")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(new EnregistrerConsultationRequest(offreId, espaceId, null))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            LOG.warn("Échec de l'enregistrement de la consultation (offreId={}, espaceId={}) : {}", offreId, espaceId, e.getMessage());
        }
    }

    public List<HistoriqueConsultationResponse> listerHistorique(String accessToken) {
        LOG.info("Requête GET {}/api/v1/historique/consultations", baseUrl);
        try {
            return client.get().uri("/api/v1/historique/consultations")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<HistoriqueConsultationResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public void effacerHistorique(String accessToken) {
        LOG.info("Requête DELETE {}/api/v1/historique/consultations", baseUrl);
        try {
            client.delete().uri("/api/v1/historique/consultations")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
