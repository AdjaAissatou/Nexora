package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.EnTetesClient;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.FicheEspace;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.MotifRequest;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.OffreModeree;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.PageEspaces;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.PageOffres;
import sn.ucad.nexora.web.error.ApiErrors;
import sn.ucad.nexora.web.error.ApiException;

/**
 * Client de la modération (§9.9) : espaces (espace-service) et offres (catalogue-service), via l'API
 * Gateway. Bean CDI pour être injecté directement dans les managed beans JSF.
 */
@ApplicationScoped
public class ModerationApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(ModerationApiClient.class);
    private final RestClient espaces = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/espace-service")
            .requestInterceptor(EnTetesClient.IP_NAVIGATEUR).build();
    private final RestClient offres = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/catalogue-service")
            .requestInterceptor(EnTetesClient.IP_NAVIGATEUR).build();

    // ------------------------------------------------------------------ espaces

    public PageEspaces espaces(String accessToken, String recherche, String statut, Boolean verifie, int page) {
        LOG.info("Requête GET /espace-service/api/v1/admin/espaces (page {})", page);
        return appel(() -> espaces.get()
                .uri(u -> u.path("/api/v1/admin/espaces")
                        .queryParamIfPresent("recherche", non(recherche))
                        .queryParamIfPresent("statut", non(statut))
                        .queryParamIfPresent("verifie", Optional.ofNullable(verifie))
                        .queryParam("page", page).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(PageEspaces.class));
    }

    public FicheEspace ficheEspace(String accessToken, Long id) {
        LOG.info("Requête GET /espace-service/api/v1/admin/espaces/{}", id);
        return appel(() -> espaces.get().uri("/api/v1/admin/espaces/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(FicheEspace.class));
    }

    public FicheEspace suspendreEspace(String accessToken, Long id, String motif) {
        return decisionEspace(accessToken, id, "suspendre", motif);
    }

    public FicheEspace reactiverEspace(String accessToken, Long id, String motif) {
        return decisionEspace(accessToken, id, "reactiver", motif);
    }

    private FicheEspace decisionEspace(String accessToken, Long id, String decision, String motif) {
        LOG.info("Requête POST /espace-service/api/v1/admin/espaces/{}/{}", id, decision);
        return appel(() -> espaces.post().uri("/api/v1/admin/espaces/{id}/" + decision, id)
                .header("Authorization", "Bearer " + accessToken)
                .body(new MotifRequest(motif))
                .retrieve().body(FicheEspace.class));
    }

    // ------------------------------------------------------------------ offres

    public PageOffres offres(String accessToken, String recherche, String statut, Long idEspace, int page) {
        LOG.info("Requête GET /catalogue-service/api/v1/admin/offres (page {})", page);
        return appel(() -> offres.get()
                .uri(u -> u.path("/api/v1/admin/offres")
                        .queryParamIfPresent("recherche", non(recherche))
                        .queryParamIfPresent("statut", non(statut))
                        .queryParamIfPresent("idEspace", Optional.ofNullable(idEspace))
                        .queryParam("page", page).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(PageOffres.class));
    }

    public OffreModeree suspendreOffre(String accessToken, Long id, String motif) {
        return decisionOffre(accessToken, id, "suspendre", motif);
    }

    public OffreModeree republierOffre(String accessToken, Long id, String motif) {
        return decisionOffre(accessToken, id, "republier", motif);
    }

    private OffreModeree decisionOffre(String accessToken, Long id, String decision, String motif) {
        LOG.info("Requête POST /catalogue-service/api/v1/admin/offres/{}/{}", id, decision);
        return appel(() -> offres.post().uri("/api/v1/admin/offres/{id}/" + decision, id)
                .header("Authorization", "Bearer " + accessToken)
                .body(new MotifRequest(motif))
                .retrieve().body(OffreModeree.class));
    }

    // ------------------------------------------------------------------ avis et signalements (recherche-service)

    private final RestClient recherche = RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/recherche-service")
            .requestInterceptor(EnTetesClient.IP_NAVIGATEUR).build();

    public sn.ucad.nexora.web.dto.administration.ModerationDtos.PageAvis avis(String accessToken, String recherche, String etat,
                                                                              Long idEspace, int page) {
        LOG.info("Requête GET /recherche-service/api/v1/admin/avis (page {})", page);
        return appel(() -> this.recherche.get()
                .uri(u -> u.path("/api/v1/admin/avis")
                        .queryParamIfPresent("recherche", non(recherche))
                        .queryParamIfPresent("etat", non(etat))
                        .queryParamIfPresent("idEspace", Optional.ofNullable(idEspace))
                        .queryParam("page", page).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(sn.ucad.nexora.web.dto.administration.ModerationDtos.PageAvis.class));
    }

    public sn.ucad.nexora.web.dto.administration.ModerationDtos.AvisModere masquerAvis(String accessToken, Long id, String motif) {
        return decisionRecherche(accessToken, "/api/v1/admin/avis/{id}/masquer", id, motif,
                sn.ucad.nexora.web.dto.administration.ModerationDtos.AvisModere.class);
    }

    public sn.ucad.nexora.web.dto.administration.ModerationDtos.AvisModere retablirAvis(String accessToken, Long id, String motif) {
        return decisionRecherche(accessToken, "/api/v1/admin/avis/{id}/retablir", id, motif,
                sn.ucad.nexora.web.dto.administration.ModerationDtos.AvisModere.class);
    }

    public sn.ucad.nexora.web.dto.administration.ModerationDtos.PageSignalements signalements(String accessToken, String statut,
                                                                                              String type, int page) {
        LOG.info("Requête GET /recherche-service/api/v1/admin/signalements (page {})", page);
        return appel(() -> this.recherche.get()
                .uri(u -> u.path("/api/v1/admin/signalements")
                        .queryParamIfPresent("statut", non(statut))
                        .queryParamIfPresent("type", non(type))
                        .queryParam("page", page).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(sn.ucad.nexora.web.dto.administration.ModerationDtos.PageSignalements.class));
    }

    public sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail signalement(String accessToken, Long id) {
        LOG.info("Requête GET /recherche-service/api/v1/admin/signalements/{}", id);
        return appel(() -> this.recherche.get().uri("/api/v1/admin/signalements/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail.class));
    }

    public sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail prendreSignalement(String accessToken, Long id) {
        return decisionRecherche(accessToken, "/api/v1/admin/signalements/{id}/prendre", id, null,
                sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail.class);
    }

    /** {@code fonde} : TRAITE (une suite a été donnée) ou REJETE. */
    public sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail clore(String accessToken, Long id, boolean fonde,
                                                                                        String commentaire) {
        return decisionRecherche(accessToken, "/api/v1/admin/signalements/{id}/" + (fonde ? "traiter" : "rejeter"), id,
                commentaire, sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail.class);
    }

    private <T> T decisionRecherche(String accessToken, String chemin, Long id, String motif, Class<T> type) {
        LOG.info("Requête POST /recherche-service{} ({})", chemin, id);
        return appel(() -> this.recherche.post().uri(chemin, id)
                .header("Authorization", "Bearer " + accessToken)
                .body(new MotifRequest(motif))
                .retrieve().body(type));
    }

    private static Optional<String> non(String s) {
        return Optional.ofNullable(s).filter(v -> !v.isBlank());
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
