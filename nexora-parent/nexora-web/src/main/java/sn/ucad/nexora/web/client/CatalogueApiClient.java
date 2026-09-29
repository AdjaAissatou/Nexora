package sn.ucad.nexora.web.client;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import sn.ucad.nexora.web.config.GatewayConfig;
import sn.ucad.nexora.web.dto.catalogue.AttributResponse;
import sn.ucad.nexora.web.dto.catalogue.CategorieResponse;
import sn.ucad.nexora.web.dto.catalogue.CreateOffreRequest;
import sn.ucad.nexora.web.dto.catalogue.CreateOffreResponse;
import sn.ucad.nexora.web.dto.catalogue.LieuPublicResponse;
import sn.ucad.nexora.web.dto.catalogue.OffreDetailResponse;
import sn.ucad.nexora.web.dto.catalogue.OffreEditionResponse;
import sn.ucad.nexora.web.dto.catalogue.OffrePageResponse;
import sn.ucad.nexora.web.dto.catalogue.TypeOffreResponse;
import sn.ucad.nexora.web.dto.catalogue.UpdateOffreRequest;
import sn.ucad.nexora.web.error.ApiErrors;

/**
 * Client de catalogue-service (via l'API Gateway) : recherche et fiche détaillée d'une offre.
 * Bean CDI (et non Spring) pour rester injectable directement dans les managed beans JSF.
 */
@ApplicationScoped
public class CatalogueApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogueApiClient.class);
    private final String baseUrl = GatewayConfig.gatewayUrl() + "/catalogue-service";
    private final RestClient client = RestClient.builder().baseUrl(baseUrl).build();

    public OffrePageResponse rechercher(CritereRecherche c) {
        LOG.info("Requête GET {}/api/v1/offres/recherche ({})", baseUrl, c);
        try {
            return client
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/offres/recherche")
                            .queryParamIfPresent("q", java.util.Optional.ofNullable(vide(c.q())))
                            .queryParamIfPresent("categorie", java.util.Optional.ofNullable(vide(c.categorie())))
                            .queryParamIfPresent("idCategorie", java.util.Optional.ofNullable(c.idCategorie()))
                            .queryParamIfPresent("typeEspace", java.util.Optional.ofNullable(vide(c.typeEspace())))
                            .queryParamIfPresent("commune", java.util.Optional.ofNullable(vide(c.commune())))
                            .queryParamIfPresent("region", java.util.Optional.ofNullable(vide(c.region())))
                            .queryParamIfPresent("idEspace", java.util.Optional.ofNullable(c.idEspace()))
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
        LOG.info("Requête GET {}/api/v1/offres/{}", baseUrl, id);
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

    public List<CategorieResponse> categoriesRacines() {
        return categoriesRacines(null);
    }

    public List<CategorieResponse> categoriesRacines(Long idTypeEspace) {
        LOG.info("Requête GET {}/api/v1/categories (typeEspace={})", baseUrl, idTypeEspace);
        try {
            return client.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/categories")
                            .queryParamIfPresent("typeEspace", java.util.Optional.ofNullable(idTypeEspace))
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<CategorieResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<CategorieResponse> sousCategories(Long idCategorie) {
        LOG.info("Requête GET {}/api/v1/categories/{}/sous-categories", baseUrl, idCategorie);
        try {
            return client.get().uri("/api/v1/categories/{id}/sous-categories", idCategorie).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<CategorieResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<TypeOffreResponse> typesOffre(Long idCategorie) {
        LOG.info("Requête GET {}/api/v1/categories/{}/types-offre", baseUrl, idCategorie);
        try {
            return client.get().uri("/api/v1/categories/{id}/types-offre", idCategorie).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<TypeOffreResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<AttributResponse> attributs(Long idCategorie) {
        LOG.info("Requête GET {}/api/v1/categories/{}/attributs", baseUrl, idCategorie);
        try {
            return client.get().uri("/api/v1/categories/{id}/attributs", idCategorie).retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<AttributResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public CreateOffreResponse creerOffre(String accessToken, CreateOffreRequest requete) {
        LOG.info("Requête POST {}/api/v1/offres", baseUrl);
        try {
            return client.post().uri("/api/v1/offres")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requete)
                    .retrieve()
                    .body(CreateOffreResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public OffreEditionResponse obtenirEdition(String accessToken, Long idOffre) {
        LOG.info("Requête GET {}/api/v1/offres/{}/edition", baseUrl, idOffre);
        try {
            return client.get().uri("/api/v1/offres/{id}/edition", idOffre)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(OffreEditionResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public CreateOffreResponse modifierOffre(String accessToken, Long idOffre, UpdateOffreRequest requete) {
        LOG.info("Requête PUT {}/api/v1/offres/{}", baseUrl, idOffre);
        try {
            return client.put().uri("/api/v1/offres/{id}", idOffre)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requete)
                    .retrieve()
                    .body(CreateOffreResponse.class);
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public void supprimerOffre(String accessToken, Long idOffre) {
        LOG.info("Requête DELETE {}/api/v1/offres/{}", baseUrl, idOffre);
        try {
            client.delete().uri("/api/v1/offres/{id}", idOffre)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public List<LieuPublicResponse> rechercherLieuxPublics(String q, String commune, int limite) {
        LOG.info("Requête GET {}/api/v1/lieux-publics/recherche (q={}, commune={})", baseUrl, q, commune);
        try {
            return client.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/lieux-publics/recherche")
                            .queryParamIfPresent("q", java.util.Optional.ofNullable(vide(q)))
                            .queryParamIfPresent("commune", java.util.Optional.ofNullable(vide(commune)))
                            .queryParam("limite", limite)
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<LieuPublicResponse>>() {});
        } catch (RestClientResponseException e) {
            throw ApiErrors.depuis(e);
        } catch (Exception e) {
            throw ApiErrors.reseau(e);
        }
    }

    public void basculerDisponibiliteOffre(String accessToken, Long idOffre, boolean disponible) {
        LOG.info("Requête PATCH {}/api/v1/offres/{}/disponibilite ({})", baseUrl, idOffre, disponible);
        try {
            client.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/offres/{id}/disponibilite")
                            .queryParam("disponible", disponible)
                            .build(idOffre))
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
