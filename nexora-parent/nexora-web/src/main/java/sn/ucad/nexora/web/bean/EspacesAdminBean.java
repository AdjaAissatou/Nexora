package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import sn.ucad.nexora.web.client.ModerationApiClient;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.PageEspaces;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Liste des espaces à modérer ({@code admin/espaces.xhtml}) : filtres dans l'URL, pagination (§9.9). */
@Named
@ViewScoped
public class EspacesAdminBean implements Serializable {

    @Inject
    private transient ModerationApiClient moderationApiClient;

    @Inject
    private SessionBean session;

    private String recherche;
    private String statut;
    /** "", "true" ou "false". */
    private String verifie;
    private int page;
    private PageEspaces resultat;
    private String erreur;

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        recherche = p.get("recherche");
        statut = p.get("statut");
        verifie = p.get("verifie");
        page = entier(p.get("page"));
        Boolean v = "true".equals(verifie) ? Boolean.TRUE : "false".equals(verifie) ? Boolean.FALSE : null;
        try {
            resultat = moderationApiClient.espaces(session.getAccessToken(), recherche, statut, v, page);
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public String filtrer() {
        return "/admin/espaces?faces-redirect=true" + param("recherche", recherche) + param("statut", statut)
                + param("verifie", verifie);
    }

    static String param(String nom, String valeur) {
        return valeur == null || valeur.isBlank() ? "" : "&" + nom + "=" + URLEncoder.encode(valeur.trim(), StandardCharsets.UTF_8);
    }

    static int entier(String s) {
        try {
            return s == null ? 0 : Math.max(0, Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public PageEspaces getResultat() { return resultat; }
    public String getErreur() { return erreur; }
    public int getPage() { return page; }
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getVerifie() { return verifie; }
    public void setVerifie(String verifie) { this.verifie = verifie; }
}
