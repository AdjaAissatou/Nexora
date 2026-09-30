package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.FicheCompte;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.MotifRequest;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.PageComptes;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.RolesEtPermissions;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de l'administration des comptes, rôles et permissions (auth-service, via l'API Gateway).
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class AdminComptesApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(AdminComptesApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/auth-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl)
            .requestInterceptor(sn.ucad.nexora.web.config.EnTetesClient.IP_NAVIGATEUR).build();

    public PageComptes rechercher(String accessToken, String recherche, String role, String etat, int page) {
        LOG.info("Requête GET {}/api/v1/admin/comptes (page {})", baseUrl, page);
        return appel(() -> client.get()
                .uri(u -> u.path("/api/v1/admin/comptes")
                        .queryParamIfPresent("recherche", non(recherche))
                        .queryParamIfPresent("role", non(role))
                        .queryParamIfPresent("etat", non(etat))
                        .queryParam("page", page).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(PageComptes.class));
    }

    public FicheCompte fiche(String accessToken, UUID accountId) {
        LOG.info("Requête GET {}/api/v1/admin/comptes/{}", baseUrl, accountId);
        return appel(() -> client.get().uri("/api/v1/admin/comptes/{id}", accountId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(FicheCompte.class));
    }

    public FicheCompte suspendre(String accessToken, UUID accountId, String motif) {
        return actionCompte(accessToken, "/api/v1/admin/comptes/{id}/suspendre", accountId, null, motif);
    }

    public FicheCompte reactiver(String accessToken, UUID accountId, String motif) {
        return actionCompte(accessToken, "/api/v1/admin/comptes/{id}/reactiver", accountId, null, motif);
    }

    public FicheCompte donnerRole(String accessToken, UUID accountId, String role, String motif) {
        return actionCompte(accessToken, "/api/v1/admin/comptes/{id}/roles/{role}", accountId, role, motif);
    }

    public FicheCompte retirerRole(String accessToken, UUID accountId, String role, String motif) {
        return actionCompte(accessToken, "/api/v1/admin/comptes/{id}/roles/{role}/retirer", accountId, role, motif);
    }

    private FicheCompte actionCompte(String accessToken, String chemin, UUID accountId, String role, String motif) {
        LOG.info("Requête POST {}{} ({})", baseUrl, chemin, accountId);
        return appel(() -> client.post().uri(chemin, accountId, role)
                .header("Authorization", "Bearer " + accessToken)
                .body(new MotifRequest(motif))
                .retrieve().body(FicheCompte.class));
    }

    public RolesEtPermissions rolesEtPermissions(String accessToken) {
        LOG.info("Requête GET {}/api/v1/admin/roles", baseUrl);
        return appel(() -> client.get().uri("/api/v1/admin/roles")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(RolesEtPermissions.class));
    }

    public RolesEtPermissions modifierPermission(String accessToken, String role, String permission, boolean accorder,
                                                 String motif) {
        String chemin = "/api/v1/admin/roles/{role}/permissions/{permission}" + (accorder ? "" : "/retirer");
        LOG.info("Requête POST {}{} ({} / {})", baseUrl, chemin, role, permission);
        return appel(() -> client.post().uri(chemin, role, permission)
                .header("Authorization", "Bearer " + accessToken)
                .body(new MotifRequest(motif))
                .retrieve().body(RolesEtPermissions.class));
    }

    private static Optional<String> non(String s) {
        return Optional.ofNullable(s).filter(v -> !v.isBlank());
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
