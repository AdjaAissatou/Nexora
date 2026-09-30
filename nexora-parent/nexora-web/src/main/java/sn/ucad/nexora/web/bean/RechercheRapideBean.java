package sn.ucad.nexora.web.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import sn.ucad.nexora.web.util.TypeEspaceVue;

/** Backing bean de la barre de recherche de la page d'accueil. */
@Named
@ViewScoped
public class RechercheRapideBean implements Serializable {

    private String q;

    /** « Où ? » : une commune (Dakar, Thiès, Saint-Louis…), transmise telle quelle à la recherche. */
    private String commune;

    public String rechercher() {
        String param = q == null ? "" : URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);
        String ou = commune == null || commune.isBlank() ? "" : "&commune=" + URLEncoder.encode(commune.trim(), StandardCharsets.UTF_8);
        return "/recherche.xhtml?q=" + param + ou + "&faces-redirect=true";
    }

    public List<TypeEspaceVue> getCategories() {
        return TypeEspaceVue.TOUS;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }
}
