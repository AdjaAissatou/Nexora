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
import sn.ucad.nexora.web.dto.administration.ModerationDtos.PageOffres;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Liste des offres à modérer ({@code admin/offres.xhtml}) : filtres dans l'URL ; suspendre ou
 * republier une offre depuis sa ligne, avec le motif saisi en haut de la liste (§9.9).
 */
@Named
@ViewScoped
public class OffresAdminBean implements Serializable {

    @Inject
    private transient ModerationApiClient moderationApiClient;

    @Inject
    private SessionBean session;

    private String recherche;
    private String statut;
    private int page;
    private PageOffres resultat;
    private String erreur;
    private String motif;

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        recherche = p.get("recherche");
        statut = p.get("statut");
        page = EspacesAdminBean.entier(p.get("page"));
        recharger();
    }

    private void recharger() {
        try {
            resultat = moderationApiClient.offres(session.getAccessToken(), recherche, statut, null, page);
            erreur = null;
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public String filtrer() {
        return "/admin/offres?faces-redirect=true" + EspacesAdminBean.param("recherche", recherche)
                + EspacesAdminBean.param("statut", statut);
    }

    public void suspendre(Long offreId) {
        executer(() -> moderationApiClient.suspendreOffre(session.getAccessToken(), offreId, motif),
                "Offre suspendue. Le professionnel a été prévenu.");
    }

    public void republier(Long offreId) {
        executer(() -> moderationApiClient.republierOffre(session.getAccessToken(), offreId, motif),
                "Offre republiée. Le professionnel a été prévenu.");
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

    public PageOffres getResultat() { return resultat; }
    public String getErreur() { return erreur; }
    public int getPage() { return page; }
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
