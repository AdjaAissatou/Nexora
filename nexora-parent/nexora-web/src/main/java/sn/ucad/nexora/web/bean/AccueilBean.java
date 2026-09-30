package sn.ucad.nexora.web.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.ucad.nexora.web.client.AdministrationApiClient;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
import sn.ucad.nexora.web.dto.administration.ChiffresPublicsResponse;
import sn.ucad.nexora.web.dto.catalogue.OffreSummaryResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.util.PhotosNexora;

/**
 * Page d'accueil : chiffres réels de Nexora et section « Près de vous » (espaces et carte), construite
 * à partir des offres publiées — chaque résumé d'offre porte déjà l'espace, sa note, son badge et ses
 * coordonnées. Rien n'est inventé : sans données, les sections concernées ne s'affichent pas.
 */
@Named
@ViewScoped
public class AccueilBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(AccueilBean.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int ESPACES_AFFICHES = 6;

    /** Un espace tel que présenté dans « Près de vous ». {@code photoInterne} : photo d'ambiance de repli. */
    public record EspaceVitrine(Long id, String nom, String typeEspace, String commune, BigDecimal note, Integer nombreAvis,
                                boolean verifie, boolean certifie, String photo, boolean photoInterne,
                                BigDecimal latitude, BigDecimal longitude) implements Serializable {}

    @Inject
    private transient AdministrationApiClient administrationApiClient;

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    private ChiffresPublicsResponse chiffres;
    private List<EspaceVitrine> espaces = List.of();

    @PostConstruct
    public void charger() {
        chiffres = administrationApiClient.chiffresPublics();
        try {
            List<OffreSummaryResponse> offres = catalogueApiClient.rechercher(new CritereRecherche(
                    null, null, null, null, null, null, null, null, null, null, null, null, "PERTINENCE", 0, 60)).contenu();
            Map<Long, EspaceVitrine> parEspace = new LinkedHashMap<>();
            for (OffreSummaryResponse o : offres) {
                if (o.espaceId() == null) continue;
                EspaceVitrine deja = parEspace.get(o.espaceId());
                boolean photoOffre = o.imagePrincipale() != null && !o.imagePrincipale().isBlank();
                if (deja == null || (deja.photoInterne() && photoOffre)) {
                    parEspace.put(o.espaceId(), new EspaceVitrine(o.espaceId(), o.espaceNom(), o.typeEspace(),
                            o.localisationCourte(), o.espaceNoteMoyenne(), o.espaceNombreAvis(), o.espaceVerifie(),
                            o.espaceCertifie(), photoOffre ? o.imagePrincipale() : PhotosNexora.pourType(o.typeEspace()),
                            !photoOffre, o.latitude(), o.longitude()));
                }
            }
            espaces = new ArrayList<>(parEspace.values());
        } catch (ApiException e) {
            LOG.warn("Espaces de l'accueil indisponibles : {}", e.getMessage());
        }
    }

    public ChiffresPublicsResponse getChiffres() {
        return chiffres;
    }

    /** Les premiers espaces pour les cartes (la carte, elle, les montre tous). */
    public List<EspaceVitrine> getEspacesAffiches() {
        return espaces.stream().limit(ESPACES_AFFICHES).toList();
    }

    /** Types d'espace présents, pour les filtres de « Près de vous ». */
    public List<String> getTypesPresents() {
        return espaces.stream().map(EspaceVitrine::typeEspace).filter(t -> t != null && !t.isBlank())
                .distinct().sorted().toList();
    }

    /** Photo d'ambiance affichée si la photo d'un espace ne se charge pas. */
    public String photoRepli(String typeEspace) {
        return PhotosNexora.pourType(typeEspace);
    }

    public boolean isPresDeVousVisible() {
        return !espaces.isEmpty();
    }

    /** Points de la carte : uniquement les espaces géolocalisés. */
    public String getPointsJson() {
        List<Map<String, Object>> points = new ArrayList<>();
        for (EspaceVitrine e : espaces) {
            if (e.latitude() == null || e.longitude() == null) continue;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", e.id());
            p.put("nom", e.nom());
            p.put("type", e.typeEspace());
            p.put("commune", e.commune());
            p.put("verifie", e.verifie());
            p.put("lat", e.latitude());
            p.put("lng", e.longitude());
            points.add(p);
        }
        try {
            return JSON.writeValueAsString(points);
        } catch (Exception ex) {
            return "[]";
        }
    }
}
