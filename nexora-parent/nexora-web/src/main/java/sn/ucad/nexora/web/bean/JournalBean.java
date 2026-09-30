package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import sn.ucad.nexora.web.client.AdministrationApiClient;
import sn.ucad.nexora.web.dto.administration.PageJournalResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Journal des actions du back-office ({@code admin/journal.xhtml}) : filtres dans l'URL, pagination. */
@Named
@ViewScoped
public class JournalBean implements Serializable {

    @Inject
    private transient AdministrationApiClient administrationApiClient;

    @Inject
    private SessionBean session;

    private String module;
    private String recherche;
    private int page;
    private PageJournalResponse resultat;
    private String erreur;

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        module = p.get("module");
        recherche = p.get("recherche");
        try {
            page = p.get("page") == null ? 0 : Math.max(0, Integer.parseInt(p.get("page")));
        } catch (NumberFormatException e) {
            page = 0;
        }
        try {
            resultat = administrationApiClient.journal(session.getAccessToken(), module, recherche, page);
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    /** Relance la recherche avec les filtres saisis (redirection GET : l'URL reste partageable). */
    public String filtrer() {
        return "/admin/journal?faces-redirect=true"
                + (module == null || module.isBlank() ? "" : "&module=" + encoder(module))
                + (recherche == null || recherche.isBlank() ? "" : "&recherche=" + encoder(recherche));
    }

    private static String encoder(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    public List<String> getModules() {
        return resultat == null ? List.of() : resultat.modules();
    }

    public PageJournalResponse getResultat() {
        return resultat;
    }

    public String getErreur() {
        return erreur;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRecherche() {
        return recherche;
    }

    public void setRecherche(String recherche) {
        this.recherche = recherche;
    }

    public int getPage() {
        return page;
    }
}
