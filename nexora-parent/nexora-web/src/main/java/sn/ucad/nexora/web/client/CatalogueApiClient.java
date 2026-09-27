package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.catalogue.OffreDetailResponse;
import sn.ucad.nexora.web.dto.catalogue.OffrePageResponse;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de catalogue-service (via l'API Gateway) : recherche et fiche détaillée d'une offre.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class CatalogueApiClient {

    private final RestClient client =
            RestClient.builder().baseUrl(GatewayConfig.gatewayUrl() + "/catalogue-service").build();

    public OffrePageResponse rechercher(CritereRecherche c) {
        try {
            return client
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/offres/recherche")
                            .queryParamIfPresent("q", java.util.Optional.ofNullable(vide(c.q())))
                            .queryParamIfPresent("categorie", java.util.Optional.ofNullable(vide(c.categorie())))
                            .queryParamIfPresent("typeEspace", java.util.Optional.ofNullable(vide(c.typeEspace())))
                            .queryParamIfPresent("commune", java.util.Optional.ofNullable(vide(c.commune())))
                            .queryParamIfPresent("region", java.util.Optional.ofNullable(vide(c.region())))
                            .queryParamIfPresent("prixMin", java.util.Optional.ofNullable(c.prixMin()))
                            .queryParamIfPresent("prixMax", java.util.Optional.ofNullable(c.prixMax()))
                            .queryParamIfPresent("estProduit", java.util.Optional.ofNullable(c.estProduit()))
                            .queryParamIfPresent("avecPromotion", java.util.Optional.ofNullable(c.avecPromotion()))
                            .queryParamIfPresent("espaceVerifie", java.util.Optional.ofNullable(c.espaceVerifie()))
                            .queryParam("tri", c.tri() == null ? "PERTINENCE" : c.tri())
                            .queryParam("page", c.page())
                            .queryParam("taille", c.taille())
                            .build())
                    .retrieve()
                    .body(OffrePageResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public OffreDetailResponse obtenir(Long id) {
        try {
            return client.get().uri("/api/v1/offres/{id}", id).retrieve().body(OffreDetailResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
