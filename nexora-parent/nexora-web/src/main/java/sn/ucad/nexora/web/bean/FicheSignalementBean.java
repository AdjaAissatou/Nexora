package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.ModerationApiClient;
import sn.ucad.nexora.web.dto.administration.ModerationDtos.SignalementDetail;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Un signalement ({@code admin/signalement.xhtml?id=}) : ce qui est signalé, par qui, pourquoi ;
 * prise en charge, puis clôture motivée (traité ou rejeté). Pour un avis, le modérateur peut le
 * masquer d'ici ; pour un espace ou une offre, la sanction se prend sur la fiche de l'espace.
 */
@Named
@ViewScoped
public class FicheSignalementBean implements Serializable {

    @Inject
    private transient ModerationApiClient moderationApiClient;

    @Inject
    private SessionBean session;

    private Long id;
    private SignalementDetail detail;
    private String erreur;
    private String commentaire;

    @PostConstruct
    public void charger() {
        try {
            id = Long.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
            detail = moderationApiClient.signalement(session.getAccessToken(), id);
        } catch (NumberFormatException e) {
            erreur = "Signalement introuvable";
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public void prendre() {
        executer(() -> detail = moderationApiClient.prendreSignalement(session.getAccessToken(), id),
                "Signalement pris en charge : il est à votre nom.", false);
    }

    public void traiter() {
        executer(() -> detail = moderationApiClient.clore(session.getAccessToken(), id, true, commentaire),
                "Signalement traité. La personne qui l'a envoyé est prévenue.", true);
    }

    public void rejeter() {
        executer(() -> detail = moderationApiClient.clore(session.getAccessToken(), id, false, commentaire),
                "Signalement rejeté. La personne qui l'a envoyé est prévenue.", true);
    }

    /** Masque l'avis signalé (motif = le commentaire saisi), sans clore le signalement. */
    public void masquerAvis() {
        executer(() -> {
            moderationApiClient.masquerAvis(session.getAccessToken(), detail.avis().id(), commentaire);
            detail = moderationApiClient.signalement(session.getAccessToken(), id);
        }, "Avis masqué : il n'apparaît plus et ne compte plus dans la note. Vous pouvez maintenant clore le signalement.", false);
    }

    private void executer(Runnable action, String succes, boolean viderCommentaire) {
        try {
            action.run();
            if (viderCommentaire) commentaire = null;
            FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_INFO, succes, null));
        } catch (ApiException e) {
            FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public SignalementDetail getDetail() { return detail; }
    public String getErreur() { return erreur; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
