package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.verification.AgentRequest;
import sn.ucad.nexora.web.dto.verification.AgentVerificationResponse;
import sn.ucad.nexora.web.dto.verification.ControleRequest;
import sn.ucad.nexora.web.dto.verification.EtatVerificationResponse;
import sn.ucad.nexora.web.dto.verification.ExamenDocumentRequest;
import sn.ucad.nexora.web.dto.verification.FichierJustificatif;
import sn.ucad.nexora.web.dto.verification.MotifRequest;
import sn.ucad.nexora.web.dto.verification.ReattributionRequest;
import sn.ucad.nexora.web.dto.verification.StatistiquesVerificationResponse;
import sn.ucad.nexora.web.dto.verification.VerificationDetailResponse;
import sn.ucad.nexora.web.dto.verification.VerificationResumeResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de la vérification des espaces (espace-service, via l'API Gateway) — docs/architecture-acteurs.md §8.6.
 * Trois familles d'appels : professionnel (son espace), agent de vérification, administrateur.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class VerificationApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/espace-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl)
            .requestInterceptor(sn.ucad.nexora.web.config.EnTetesClient.IP_NAVIGATEUR).build();

    private static final ParameterizedTypeReference<List<VerificationResumeResponse>> LISTE =
            new ParameterizedTypeReference<>() {};

    // ------------------------------------------------------------------ professionnel

    public EtatVerificationResponse etat(String accessToken, Long espaceId) {
        LOG.info("Requête GET {}/api/v1/espaces/{}/verification", baseUrl, espaceId);
        return appel(() -> client.get().uri("/api/v1/espaces/{id}/verification", espaceId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(EtatVerificationResponse.class));
    }

    public EtatVerificationResponse deposerDocument(String accessToken, Long espaceId, Long typeJustificatifId,
                                                    String nomFichier, byte[] contenu) {
        LOG.info("Requête POST {}/api/v1/espaces/{}/verification/documents (type {}, {} octets)",
                baseUrl, espaceId, typeJustificatifId, contenu.length);
        MultiValueMap<String, Object> formulaire = new LinkedMultiValueMap<>();
        formulaire.add("typeJustificatifId", String.valueOf(typeJustificatifId));
        formulaire.add("fichier", new ByteArrayResource(contenu) {
            @Override
            public String getFilename() {
                return nomFichier;
            }
        });
        return appel(() -> client.post().uri("/api/v1/espaces/{id}/verification/documents", espaceId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(formulaire)
                .retrieve().body(EtatVerificationResponse.class));
    }

    public EtatVerificationResponse retirerDocument(String accessToken, Long espaceId, Long documentId) {
        LOG.info("Requête DELETE {}/api/v1/espaces/{}/verification/documents/{}", baseUrl, espaceId, documentId);
        return appel(() -> client.delete().uri("/api/v1/espaces/{id}/verification/documents/{doc}", espaceId, documentId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(EtatVerificationResponse.class));
    }

    public EtatVerificationResponse soumettre(String accessToken, Long espaceId) {
        return actionPro(accessToken, espaceId, "soumettre");
    }

    public EtatVerificationResponse retirer(String accessToken, Long espaceId) {
        return actionPro(accessToken, espaceId, "retirer");
    }

    private EtatVerificationResponse actionPro(String accessToken, Long espaceId, String action) {
        LOG.info("Requête POST {}/api/v1/espaces/{}/verification/{}", baseUrl, espaceId, action);
        return appel(() -> client.post().uri("/api/v1/espaces/{id}/verification/{action}", espaceId, action)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(EtatVerificationResponse.class));
    }

    /** Téléchargement contrôlé par espace-service (propriétaire, agent de la demande, admin). */
    public FichierJustificatif telecharger(String accessToken, Long verificationId, Long documentId) {
        LOG.info("Requête GET {}/api/v1/verifications/{}/documents/{}/fichier", baseUrl, verificationId, documentId);
        return appel(() -> {
            ResponseEntity<byte[]> reponse = client.get()
                    .uri("/api/v1/verifications/{id}/documents/{doc}/fichier", verificationId, documentId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve().toEntity(byte[].class);
            String disposition = reponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            String nom = disposition == null ? null : ContentDisposition.parse(disposition).getFilename();
            MediaType type = reponse.getHeaders().getContentType();
            return new FichierJustificatif(nom != null ? nom : "justificatif",
                    type != null ? type.toString() : MediaType.APPLICATION_OCTET_STREAM_VALUE, reponse.getBody());
        });
    }

    // ------------------------------------------------------------------ agent de vérification

    public List<VerificationResumeResponse> file(String accessToken, String statut) {
        LOG.info("Requête GET {}/api/v1/verifications?statut={}", baseUrl, statut);
        return appel(() -> client.get()
                .uri(u -> u.path("/api/v1/verifications").queryParamIfPresent("statut", java.util.Optional.ofNullable(statut)).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(LISTE));
    }

    public VerificationDetailResponse detailAgent(String accessToken, Long id) {
        LOG.info("Requête GET {}/api/v1/verifications/{}", baseUrl, id);
        return appel(() -> client.get().uri("/api/v1/verifications/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(VerificationDetailResponse.class));
    }

    public VerificationDetailResponse prendre(String accessToken, Long id) {
        return actionAgent(accessToken, id, "prendre", null);
    }

    public VerificationDetailResponse demanderInformations(String accessToken, Long id, String motif) {
        return actionAgent(accessToken, id, "demander-infos", new MotifRequest(motif));
    }

    public VerificationDetailResponse approuver(String accessToken, Long id) {
        return actionAgent(accessToken, id, "approuver", null);
    }

    public VerificationDetailResponse refuser(String accessToken, Long id, String motif) {
        return actionAgent(accessToken, id, "refuser", new MotifRequest(motif));
    }

    public VerificationDetailResponse controler(String accessToken, Long id, String code, String resultat, String commentaire) {
        LOG.info("Requête PUT {}/api/v1/verifications/{}/controles/{}", baseUrl, id, code);
        return appel(() -> client.put().uri("/api/v1/verifications/{id}/controles/{code}", id, code)
                .header("Authorization", "Bearer " + accessToken)
                .body(new ControleRequest(resultat, commentaire))
                .retrieve().body(VerificationDetailResponse.class));
    }

    public VerificationDetailResponse examinerDocument(String accessToken, Long id, Long documentId,
                                                       String decision, String motif) {
        LOG.info("Requête PUT {}/api/v1/verifications/{}/documents/{}/examen", baseUrl, id, documentId);
        return appel(() -> client.put().uri("/api/v1/verifications/{id}/documents/{doc}/examen", id, documentId)
                .header("Authorization", "Bearer " + accessToken)
                .body(new ExamenDocumentRequest(decision, motif))
                .retrieve().body(VerificationDetailResponse.class));
    }

    private VerificationDetailResponse actionAgent(String accessToken, Long id, String action, Object corps) {
        LOG.info("Requête POST {}/api/v1/verifications/{}/{}", baseUrl, id, action);
        return appel(() -> {
            var requete = client.post().uri("/api/v1/verifications/{id}/{action}", id, action)
                    .header("Authorization", "Bearer " + accessToken);
            if (corps != null) requete = requete.body(corps);
            return requete.retrieve().body(VerificationDetailResponse.class);
        });
    }

    // ------------------------------------------------------------------ administrateur

    public List<VerificationResumeResponse> toutes(String accessToken, String statut) {
        LOG.info("Requête GET {}/api/v1/admin/verifications?statut={}", baseUrl, statut);
        return appel(() -> client.get()
                .uri(u -> u.path("/api/v1/admin/verifications").queryParamIfPresent("statut", java.util.Optional.ofNullable(statut)).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(LISTE));
    }

    public StatistiquesVerificationResponse statistiques(String accessToken) {
        LOG.info("Requête GET {}/api/v1/admin/verifications/statistiques", baseUrl);
        return appel(() -> client.get().uri("/api/v1/admin/verifications/statistiques")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(StatistiquesVerificationResponse.class));
    }

    public VerificationDetailResponse detailAdmin(String accessToken, Long id) {
        LOG.info("Requête GET {}/api/v1/admin/verifications/{}", baseUrl, id);
        return appel(() -> client.get().uri("/api/v1/admin/verifications/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(VerificationDetailResponse.class));
    }

    public VerificationDetailResponse reattribuer(String accessToken, Long id, Long agentUtilisateurId, String motif) {
        return actionAdmin(accessToken, id, "reattribuer", new ReattributionRequest(agentUtilisateurId, motif));
    }

    public VerificationDetailResponse annuler(String accessToken, Long id, String motif) {
        return actionAdmin(accessToken, id, "annuler", new MotifRequest(motif));
    }

    public VerificationDetailResponse revoquer(String accessToken, Long id, String motif) {
        return actionAdmin(accessToken, id, "revoquer", new MotifRequest(motif));
    }

    private static final ParameterizedTypeReference<List<AgentVerificationResponse>> AGENTS =
            new ParameterizedTypeReference<>() {};

    public List<AgentVerificationResponse> agents(String accessToken) {
        LOG.info("Requête GET {}/api/v1/admin/verifications/agents", baseUrl);
        return appel(() -> client.get().uri("/api/v1/admin/verifications/agents")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(AGENTS));
    }

    public List<AgentVerificationResponse> ajouterAgent(String accessToken, String email) {
        LOG.info("Requête POST {}/api/v1/admin/verifications/agents", baseUrl);
        return appel(() -> client.post().uri("/api/v1/admin/verifications/agents")
                .header("Authorization", "Bearer " + accessToken)
                .body(new AgentRequest(email))
                .retrieve().body(AGENTS));
    }

    public List<AgentVerificationResponse> retirerAgent(String accessToken, Long utilisateurId) {
        LOG.info("Requête DELETE {}/api/v1/admin/verifications/agents/{}", baseUrl, utilisateurId);
        return appel(() -> client.delete().uri("/api/v1/admin/verifications/agents/{id}", utilisateurId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().body(AGENTS));
    }

    private VerificationDetailResponse actionAdmin(String accessToken, Long id, String action, Object corps) {
        LOG.info("Requête POST {}/api/v1/admin/verifications/{}/{}", baseUrl, id, action);
        return appel(() -> client.post().uri("/api/v1/admin/verifications/{id}/{action}", id, action)
                .header("Authorization", "Bearer " + accessToken)
                .body(corps)
                .retrieve().body(VerificationDetailResponse.class));
    }

    // ------------------------------------------------------------------ outils

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
