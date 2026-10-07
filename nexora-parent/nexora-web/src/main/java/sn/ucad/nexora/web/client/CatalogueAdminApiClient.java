package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.EnTetesClient;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.administration.CatalogueAdminDtos.*;
import sn.ucad.nexora.web.error.ApiErrors;
import sn.ucad.nexora.web.error.ApiException;

/** Client de la gestion du catalogue (catalogue-service, via l'API Gateway), §9.11. */
@ApplicationScoped
public class CatalogueAdminApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogueAdminApiClient.class);
    private static final String BASE = "/api/v1/admin/catalogue";
    private final RestClient client = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/catalogue-service")
            .requestInterceptor(EnTetesClient.IP_NAVIGATEUR).build();

    public List<Noeud> racines(String jeton) {
        return appel(() -> client.get().uri(BASE + "/categories").header("Authorization", "Bearer " + jeton)
                .retrieve().body(new ParameterizedTypeReference<List<Noeud>>() {}));
    }

    public FicheCategorie fiche(String jeton, Long id) {
        return appel(() -> client.get().uri(BASE + "/categories/{id}", id).header("Authorization", "Bearer " + jeton)
                .retrieve().body(FicheCategorie.class));
    }

    public List<Resultat> rechercher(String jeton, String q) {
        return appel(() -> client.get().uri(u -> u.path(BASE + "/recherche").queryParam("q", q).build())
                .header("Authorization", "Bearer " + jeton).retrieve().body(new ParameterizedTypeReference<List<Resultat>>() {}));
    }

    /** Catégories écrites par les professionnels (« Autre… », §21), regroupées. */
    public List<Proposition> propositions(String jeton) {
        return appel(() -> client.get().uri(BASE + "/propositions").header("Authorization", "Bearer " + jeton)
                .retrieve().body(new ParameterizedTypeReference<List<Proposition>>() {}));
    }

    /** {@code action} : creer, rattacher ou ecarter ; renvoie les propositions restantes. */
    public List<Proposition> traiterProposition(String jeton, String action, Object corps) {
        LOG.info("Requête POST {}/propositions/{}", BASE, action);
        return appel(() -> client.post().uri(BASE + "/propositions/{a}", action).header("Authorization", "Bearer " + jeton)
                .body(corps).retrieve().body(new ParameterizedTypeReference<List<Proposition>>() {}));
    }

    /** POST ou PUT renvoyant la fiche (null si le serveur répond 204, ex. suppression d'une racine). */
    public FicheCategorie envoyer(String jeton, String methode, String chemin, Object corps, Object... variables) {
        LOG.info("Requête {} {}{}", methode, BASE, chemin);
        return appel(() -> {
            var requete = ("PUT".equals(methode) ? client.put() : client.post()).uri(BASE + chemin, variables)
                    .header("Authorization", "Bearer " + jeton);
            var r = (corps == null ? requete : requete.body(corps)).retrieve().toEntity(FicheCategorie.class);
            return r.getBody();
        });
    }

    private static <T> T appel(java.util.function.Supplier<T> requete) {
        try {
            return requete.get();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }
}
