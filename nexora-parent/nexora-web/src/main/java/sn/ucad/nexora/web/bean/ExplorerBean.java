package sn.ucad.nexora.web.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
import sn.ucad.nexora.web.dto.catalogue.ExplorerResponse;
import sn.ucad.nexora.web.dto.catalogue.OffreSummaryResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.util.FormatBean;

/**
 * Page « Explorer » (§15) : se promener dans ce que propose Nexora sans savoir exactement ce qu'on
 * cherche — par rayon (catégorie), par quartier, autour des lieux connus, sur la carte, et les
 * nouveautés, promotions et commerces ouverts. La recherche, elle, répond à une question précise.
 */
@Named
@ViewScoped
public class ExplorerBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorerBean.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int VITRINE = 8;
    /** Couleurs des pastilles de rayon quand la catégorie n'en a pas. */
    private static final List<String> PALETTE = List.of("#0F5B4F", "#C96F4A", "#C99632", "#1F5FBF", "#6A1B9A",
            "#2E7D32", "#B3562F", "#0E7490", "#9D174D", "#4D7C0F", "#7C2D12");

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private FormatBean format;

    private ExplorerResponse vue = ExplorerResponse.vide();
    private List<OffreSummaryResponse> nouveautes = List.of();
    private List<OffreSummaryResponse> promotions = List.of();
    private List<OffreSummaryResponse> ouverts = List.of();

    @PostConstruct
    public void charger() {
        try {
            vue = catalogueApiClient.explorer();
        } catch (ApiException e) {
            LOG.warn("Explorer indisponible : {}", e.getMessage());
        }
        nouveautes = offres("DATE_DESC", null, null);
        promotions = offres("PERTINENCE", Boolean.TRUE, null);
        ouverts = offres("NOTE", null, Boolean.TRUE);
    }

    private List<OffreSummaryResponse> offres(String tri, Boolean avecPromotion, Boolean ouvert) {
        try {
            return catalogueApiClient.rechercher(new CritereRecherche(null, null, null, null, null, null, null, null, null,
                    null, avecPromotion, null, ouvert, tri, 0, VITRINE)).contenu();
        } catch (ApiException e) {
            return List.of();
        }
    }

    public String couleur(ExplorerResponse.CategorieExploree c, int index) {
        if (c.couleur() != null && c.couleur().matches("#[0-9A-Fa-f]{6}")) return c.couleur();
        return PALETTE.get(index % PALETTE.size());
    }

    public long getNombreOffres() {
        return vue.categories().stream().mapToLong(ExplorerResponse.CategorieExploree::nombreOffres).sum();
    }

    public int getNombreEspaces() {
        return vue.espaces().size();
    }

    /** Espaces et lieux publics à placer sur la carte. */
    public String getPointsCarteJson() {
        List<Map<String, Object>> points = new ArrayList<>();
        for (ExplorerResponse.EspaceSurCarte e : vue.espaces()) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("lat", e.latitude());
            p.put("lng", e.longitude());
            p.put("nom", e.nom());
            p.put("categorie", "ESPACE");
            p.put("detail", e.typeEspace() + " · " + e.commune() + " · " + e.nombreOffres() + " article(s)");
            p.put("href", "/espace.xhtml?id=" + e.id());
            p.put("lien", "Voir l'espace");
            points.add(p);
        }
        for (ExplorerResponse.LieuExplore l : vue.lieux()) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("lat", l.latitude());
            p.put("lng", l.longitude());
            p.put("nom", l.nom());
            p.put("categorie", "LIEU");
            p.put("detail", format.typeLieu(l.typeLieu()));
            p.put("href", "/recherche.xhtml?lieu=" + l.id());
            p.put("lien", "Commerces autour");
            points.add(p);
        }
        try {
            return JSON.writeValueAsString(points);
        } catch (Exception e) {
            return "[]";
        }
    }

    public ExplorerResponse getVue() { return vue; }
    public List<OffreSummaryResponse> getNouveautes() { return nouveautes; }
    public List<OffreSummaryResponse> getPromotions() { return promotions; }
    public List<OffreSummaryResponse> getOuverts() { return ouverts; }
}
