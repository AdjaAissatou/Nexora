package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import sn.ucad.nexora.web.client.AdminComptesApiClient;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.FicheCompte;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Fiche d'un compte dans le back-office ({@code admin/utilisateur.xhtml?id=}) : rôles, permissions
 * effectives, espaces, historique ; suspension, réactivation et rôles administratifs, toujours motivés.
 */
@Named
@ViewScoped
public class FicheUtilisateurBean implements Serializable {

    /** Rôles donnés à la main (les autres ont leur propre cycle de vie, §9.2). */
    private static final List<String> ROLES_ADMINISTRATIFS = List.of("SUPER_ADMIN", "ADMIN", "MODERATEUR", "SUPPORT", "GESTIONNAIRE");

    @Inject
    private transient AdminComptesApiClient adminComptesApiClient;

    @Inject
    private SessionBean session;

    private UUID id;
    private FicheCompte fiche;
    private String erreur;
    private String motif;

    @PostConstruct
    public void charger() {
        try {
            id = UUID.fromString(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
            fiche = adminComptesApiClient.fiche(session.getAccessToken(), id);
        } catch (IllegalArgumentException | NullPointerException e) {
            erreur = "Compte introuvable";
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public void suspendre() {
        executer(() -> adminComptesApiClient.suspendre(session.getAccessToken(), id, motif),
                "Compte suspendu. Sa session sera fermée au plus tard dans 15 minutes.");
    }

    public void reactiver() {
        executer(() -> adminComptesApiClient.reactiver(session.getAccessToken(), id, motif), "Compte réactivé.");
    }

    public void donnerRole(String role) {
        executer(() -> adminComptesApiClient.donnerRole(session.getAccessToken(), id, role, motif),
                "Rôle " + role + " donné. Il s'applique à la prochaine connexion du compte, ou au plus tard dans 15 minutes.");
    }

    public void retirerRole(String role) {
        executer(() -> adminComptesApiClient.retirerRole(session.getAccessToken(), id, role, motif),
                "Rôle " + role + " retiré. Effet au plus tard dans 15 minutes.");
    }

    private void executer(Supplier<FicheCompte> action, String succes) {
        try {
            fiche = action.get();
            motif = null;
            message(FacesMessage.SEVERITY_INFO, succes);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(gravite, texte, null));
    }

    /** Vrai si la fiche est celle du compte connecté : aucune action possible sur soi-même. */
    public boolean isSoiMeme() {
        return fiche != null && session.getCompte() != null && fiche.compte().accountId().equals(session.getCompte().id());
    }

    public boolean isSuspendu() {
        return fiche != null && "SUSPENDU".equals(fiche.compte().etat());
    }

    public List<String> getRolesAdministratifsDonnables() {
        return fiche == null ? List.of() : ROLES_ADMINISTRATIFS.stream().filter(r -> !fiche.compte().roles().contains(r)).toList();
    }

    public List<String> getRolesAdministratifsDetenus() {
        return fiche == null ? List.of() : ROLES_ADMINISTRATIFS.stream().filter(r -> fiche.compte().roles().contains(r)).toList();
    }

    public FicheCompte getFiche() { return fiche; }
    public String getErreur() { return erreur; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
