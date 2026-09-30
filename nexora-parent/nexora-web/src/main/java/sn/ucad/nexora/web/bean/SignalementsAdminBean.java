package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;
import sn.ucad.nexora.web.client.ModerationApiClient;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.PageSignalements;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * File des signalements ({@code admin/signalements.xhtml}). Sans filtre choisi, elle montre ce qui
 * attend une décision (EN_ATTENTE) ; « tous » affiche aussi les signalements clos (§9.10).
 */
@Named
@ViewScoped
public class SignalementsAdminBean implements Serializable {

    @Inject
    private transient ModerationApiClient moderationApiClient;

    @Inject
    private SessionBean session;

    private String statut;
    private String type;
    private int page;
    private PageSignalements resultat;
    private String erreur;

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        statut = p.containsKey("statut") ? p.get("statut") : "EN_ATTENTE";
        type = p.get("type");
        page = EspacesAdminBean.entier(p.get("page"));
        try {
            resultat = moderationApiClient.signalements(session.getAccessToken(), "TOUS".equals(statut) ? null : statut, type, page);
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public String filtrer() {
        return "/admin/signalements?faces-redirect=true" + EspacesAdminBean.param("statut", statut == null || statut.isBlank() ? "TOUS" : statut)
                + EspacesAdminBean.param("type", type);
    }

    public PageSignalements getResultat() { return resultat; }
    public String getErreur() { return erreur; }
    public int getPage() { return page; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
