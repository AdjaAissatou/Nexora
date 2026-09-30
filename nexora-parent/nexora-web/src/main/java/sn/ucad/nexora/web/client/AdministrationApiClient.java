package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.administration.ChiffresPublicsResponse;
import sn.ucad.nexora.web.dto.administration.PageJournalResponse;
import sn.ucad.nexora.web.dto.administration.TableauDeBordResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client d'administration-service (via l'API Gateway) : tableau de bord et journal du back-office.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class AdministrationApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(AdministrationApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/administration-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl)
            .requestInterceptor(sn.ucad.nexora.web.config.EnTetesClient.IP_NAVIGATEUR).build();

    /** Chiffres publics de l'accueil ; {@code null} si indisponibles (la page n'affiche alors aucun chiffre). */
    public ChiffresPublicsResponse chiffresPublics() {
        try {
            return client.get().uri("/api/v1/public/chiffres").retrieve().body(ChiffresPublicsResponse.class);
        } catch (Exception e) {
            LOG.warn("Chiffres publics indisponibles : {}", e.getMessage());
            return null;
        }
    }

    public TableauDeBordResponse tableauDeBord(String accessToken) {
        LOG.info("Requête GET {}/api/v1/admin/tableau-de-bord", baseUrl);
        try {
            return client.get().uri("/api/v1/admin/tableau-de-bord")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve().body(TableauDeBordResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public PageJournalResponse journal(String accessToken, String module, String recherche, int page) {
        LOG.info("Requête GET {}/api/v1/admin/journal (module={}, page={})", baseUrl, module, page);
        try {
            return client.get()
                    .uri(u -> u.path("/api/v1/admin/journal")
                            .queryParamIfPresent("module", Optional.ofNullable(module).filter(s -> !s.isBlank()))
                            .queryParamIfPresent("recherche", Optional.ofNullable(recherche).filter(s -> !s.isBlank()))
                            .queryParam("page", page)
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve().body(PageJournalResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
