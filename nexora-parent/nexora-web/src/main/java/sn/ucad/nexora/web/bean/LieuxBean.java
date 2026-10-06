package sn.ucad.nexora.web.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.dto.catalogue.LieuPublicResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.util.FormatBean;

/**
 * « Lieux et repères » (§20) : marchés, quartiers, sites célèbres, gares, hôpitaux… — chacun avec
 * l'itinéraire et les commerces autour. Filtres par famille et par texte, côté navigateur.
 */
@Named
@ViewScoped
public class LieuxBean implements Serializable {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Familles de lieux affichées en filtres : libellé → types de lieu. */
    public record Famille(String code, String libelle, List<String> types) implements Serializable {}

    public static final List<Famille> FAMILLES = List.of(
            new Famille("visiter", "⭐ À visiter", List.of("TOURISME", "MONUMENT", "MUSEE", "CULTURE", "PARC", "PLAGE")),
            new Famille("marches", "🛒 Marchés et shopping", List.of("MARCHE", "CENTRE_COMMERCIAL")),
            new Famille("quartiers", "🏘️ Quartiers", List.of("QUARTIER")),
            new Famille("transports", "🚆 Transports", List.of("GARE", "GARE_ROUTIERE", "AEROPORT", "PORT")),
            new Famille("sante", "🏥 Santé", List.of("HOPITAL", "PHARMACIE")),
            new Famille("culte", "🕌 Lieux de culte", List.of("MOSQUEE", "EGLISE")),
            new Famille("services", "🏛️ Administrations et services", List.of("ADMINISTRATION", "BANQUE", "UNIVERSITE")),
            new Famille("sport", "⚽ Sport", List.of("STADE")));

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private FormatBean format;

    private List<LieuPublicResponse> lieux = List.of();

    @PostConstruct
    public void charger() {
        try {
            List<LieuPublicResponse> tous = new ArrayList<>(catalogueApiClient.rechercherLieuxPublics(null, null, 500));
            // La région de Dakar d'abord, puis par nom
            tous.sort(Comparator.comparing((LieuPublicResponse l) -> !"Dakar".equalsIgnoreCase(l.region()))
                    .thenComparing(LieuPublicResponse::nom, String.CASE_INSENSITIVE_ORDER));
            lieux = tous;
        } catch (ApiException e) {
            lieux = List.of();
        }
    }

    /** Code de famille d'un type de lieu (pour le filtre). */
    public String famille(String type) {
        return FAMILLES.stream().filter(f -> f.types().contains(type)).map(Famille::code).findFirst().orElse("autres");
    }

    public String getPointsCarteJson() {
        List<Map<String, Object>> points = new ArrayList<>();
        for (LieuPublicResponse l : lieux) {
            if (l.latitude() == null || l.longitude() == null) continue;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("lat", l.latitude());
            p.put("lng", l.longitude());
            p.put("nom", l.nom());
            p.put("categorie", "LIEU");
            p.put("detail", format.typeLieu(l.typeLieu()) + (l.commune() != null ? " · " + l.commune() : ""));
            p.put("href", format.lienItineraire(l.latitude(), l.longitude()));
            p.put("lien", "Itinéraire");
            points.add(p);
        }
        try {
            return JSON.writeValueAsString(points);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<LieuPublicResponse> getLieux() { return lieux; }
    public List<Famille> getFamilles() { return FAMILLES; }
}
