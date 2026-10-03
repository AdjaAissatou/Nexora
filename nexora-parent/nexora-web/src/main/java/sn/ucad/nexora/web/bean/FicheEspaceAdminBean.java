package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.ModerationApiClient;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.FicheEspace;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.AccesAdminBean;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Fiche de modération d'un espace ({@code admin/espace.xhtml?id=}) : informations, propriétaire,
 * offres, historique ; suspendre ou réactiver l'espace et ses offres, toujours avec un motif (§9.9).
 */
@Named
@ViewScoped
public class FicheEspaceAdminBean implements Serializable {

    @Inject
    private transient ModerationApiClient moderationApiClient;

    @Inject
    private SessionBean session;

    @Inject
    private AccesAdminBean acces;

    private Long id;
    private FicheEspace fiche;
    private String erreur;
    private String motif;

    @PostConstruct
    public void charger() {
        try {
            id = Long.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
            fiche = moderationApiClient.ficheEspace(session.getAccessToken(), id);
        } catch (NumberFormatException e) {
            erreur = "Espace introuvable";
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public void suspendre() {
        executer(() -> fiche = moderationApiClient.suspendreEspace(session.getAccessToken(), id, motif),
                "Espace suspendu : il n'apparaît plus dans la recherche. Le professionnel a été prévenu.");
    }

    public void reactiver() {
        executer(() -> fiche = moderationApiClient.reactiverEspace(session.getAccessToken(), id, motif),
                "Espace réactivé : il est de nouveau visible. Le professionnel a été prévenu.");
    }

    public void suspendreOffre(Long offreId) {
        executer(() -> {
            moderationApiClient.suspendreOffre(session.getAccessToken(), offreId, motif);
            fiche = moderationApiClient.ficheEspace(session.getAccessToken(), id);
        }, "Offre suspendue. Le professionnel a été prévenu.");
    }

    public void republierOffre(Long offreId) {
        executer(() -> {
            moderationApiClient.republierOffre(session.getAccessToken(), offreId, motif);
            fiche = moderationApiClient.ficheEspace(session.getAccessToken(), id);
        }, "Offre republiée. Le professionnel a été prévenu.");
    }

    private void executer(Runnable action, String succes) {
        try {
            action.run();
            motif = null;
            message(FacesMessage.SEVERITY_INFO, succes);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(gravite, texte, null));
    }

    public boolean isSuspendu() {
        return fiche != null && "SUSPENDU".equals(fiche.espace().statut());
    }

    public boolean isActif() {
        return fiche != null && "ACTIF".equals(fiche.espace().statut());
    }

    /** Un modérateur ne statue pas sur son propre espace (le service le refuse aussi). */
    public boolean isSonPropreEspace() {
        return fiche != null && session.getCompte() != null
                && session.getCompte().id().equals(fiche.espace().proprietaireCompte());
    }

    public boolean isPeutAgirSurOffres() {
        return acces.isModererOffres() && !isSonPropreEspace();
    }

    public FicheEspace getFiche() { return fiche; }
    public String getErreur() { return erreur; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
