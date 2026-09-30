package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;
import sn.ucad.nexora.web.client.ModerationApiClient;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.PageAvis;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Modération des avis ({@code admin/avis.xhtml}) : recherche, masquer ou rétablir avec un motif (§9.10). */
@Named
@ViewScoped
public class AvisAdminBean implements Serializable {

    @Inject
    private transient ModerationApiClient moderationApiClient;

    @Inject
    private SessionBean session;

    private String recherche;
    private String etat;
    private int page;
    private PageAvis resultat;
    private String erreur;
    private String motif;

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        recherche = p.get("recherche");
        etat = p.get("etat");
        page = EspacesAdminBean.entier(p.get("page"));
        recharger();
    }

    private void recharger() {
        try {
            resultat = moderationApiClient.avis(session.getAccessToken(), recherche, etat, null, page);
            erreur = null;
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public String filtrer() {
        return "/admin/avis?faces-redirect=true" + EspacesAdminBean.param("recherche", recherche) + EspacesAdminBean.param("etat", etat);
    }

    public void masquer(Long id) {
        executer(() -> moderationApiClient.masquerAvis(session.getAccessToken(), id, motif),
                "Avis masqué. Son auteur a été prévenu ; la note de l'espace est recalculée.");
    }

    public void retablir(Long id) {
        executer(() -> moderationApiClient.retablirAvis(session.getAccessToken(), id, motif),
                "Avis rétabli. Son auteur a été prévenu ; la note de l'espace est recalculée.");
    }

    private void executer(Runnable action, String succes) {
        try {
            action.run();
            motif = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, succes, null));
            recharger();
        } catch (ApiException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public PageAvis getResultat() { return resultat; }
    public String getErreur() { return erreur; }
    public int getPage() { return page; }
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
