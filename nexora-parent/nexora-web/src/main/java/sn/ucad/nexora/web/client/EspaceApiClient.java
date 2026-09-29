package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.espace.CommuneResponse;
import sn.ucad.nexora.web.dto.espace.CreateEspaceRequest;
import sn.ucad.nexora.web.dto.espace.DepartementResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.espace.RegionResponse;
import sn.ucad.nexora.web.dto.espace.TypeEspaceResponse;
import sn.ucad.nexora.web.dto.espace.UpdateEspaceRequest;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de espace-service (via l'API Gateway) : catégories, espace professionnel du compte connecté.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class EspaceApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(EspaceApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/espace-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl).build();

    public List<TypeEspaceResponse> listerTypes() {
        LOG.info("Requête GET {}/api/v1/types-espaces", baseUrl);
        try {
            return client
                    .get()
                    .uri("/api/v1/types-espaces")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<TypeEspaceResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<EspaceResponse> mesEspaces(String accessToken) {
        LOG.info("Requête GET {}/api/v1/espaces/me", baseUrl);
        try {
            return client
                    .get()
                    .uri("/api/v1/espaces/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<EspaceResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public EspaceResponse obtenir(Long id) {
        LOG.info("Requête GET {}/api/v1/espaces/{}", baseUrl, id);
        try {
            return client.get().uri("/api/v1/espaces/{id}", id).retrieve().body(EspaceResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public EspaceResponse creer(String accessToken, CreateEspaceRequest requete) {
        LOG.info("Requête POST {}/api/v1/espaces", baseUrl);
        try {
            return client
                    .post()
                    .uri("/api/v1/espaces")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requete)
                    .retrieve()
                    .body(EspaceResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public EspaceResponse mettreAJour(String accessToken, Long id, UpdateEspaceRequest requete) {
        LOG.info("Requête PUT {}/api/v1/espaces/{}", baseUrl, id);
        try {
            return client
                    .put()
                    .uri("/api/v1/espaces/{id}", id)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requete)
                    .retrieve()
                    .body(EspaceResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<RegionResponse> regions() {
        LOG.info("Requête GET {}/api/v1/geo/regions", baseUrl);
        try {
            return client.get().uri("/api/v1/geo/regions").retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<RegionResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<DepartementResponse> departements(Long idRegion) {
        LOG.info("Requête GET {}/api/v1/geo/regions/{}/departements", baseUrl, idRegion);
        try {
            return client.get().uri("/api/v1/geo/regions/{id}/departements", idRegion).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<DepartementResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<CommuneResponse> communes(Long idDepartement) {
        LOG.info("Requête GET {}/api/v1/geo/departements/{}/communes", baseUrl, idDepartement);
        try {
            return client.get().uri("/api/v1/geo/departements/{id}/communes", idDepartement).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<CommuneResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
