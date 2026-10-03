package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sn.ucad.nexora.web.client.VerificationApiClient;
import sn.ucad.nexora.web.dto.verification.AgentVerificationResponse;
import sn.ucad.nexora.web.dto.verification.EvenementVerificationResponse;
import sn.ucad.nexora.web.dto.verification.StatistiquesVerificationResponse;
import sn.ucad.nexora.web.dto.verification.VerificationDetailResponse;
import sn.ucad.nexora.web.dto.verification.VerificationResumeResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;
import sn.ucad.nexora.web.util.TelechargementJustificatif;

/**
 * Supervision de la vérification par l'administrateur ({@code admin/verifications.xhtml}) : toutes
 * les demandes, statistiques, réattribution, annulation, révocation, gestion des agents. Il ne
 * décide pas à la place de l'agent (docs/architecture-acteurs.md §8.1).
 */
@Named
@ViewScoped
public class SupervisionVerificationBean implements Serializable {

    private static final Set<String> OUVERTES_REATTRIBUABLES = Set.of("EN_ATTENTE", "EN_COURS", "A_COMPLETER");
    private static final Set<String> OUVERTES = Set.of("BROUILLON", "EN_ATTENTE", "EN_COURS", "A_COMPLETER");

    @Inject
    private transient VerificationApiClient verificationApiClient;

    @Inject
    private SessionBean session;

    private String filtreStatut;
    private List<VerificationResumeResponse> demandes = List.of();
    private StatistiquesVerificationResponse statistiques;
    private List<AgentVerificationResponse> agents = List.of();
    private VerificationDetailResponse selection;
    private String erreur;

    // Saisies
    private Long agentChoisi;
    private String motif;
    private String emailNouvelAgent;

    @PostConstruct
    public void charger() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String statut = params.get("statut");
        filtreStatut = statut == null || statut.isBlank() ? null : statut;
        rechargerListes();
        String id = params.get("id");
        if (id != null) {
            try {
                selection = verificationApiClient.detailAdmin(session.getAccessToken(), Long.valueOf(id));
            } catch (NumberFormatException | ApiException e) {
                erreur = "Demande introuvable";
            }
        }
    }

    private void rechargerListes() {
        try {
            demandes = verificationApiClient.toutes(session.getAccessToken(), filtreStatut);
            statistiques = verificationApiClient.statistiques(session.getAccessToken());
            agents = verificationApiClient.agents(session.getAccessToken());
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    // ------------------------------------------------------------------ actions sur la demande sélectionnée

    public void reattribuer() {
        executer(() -> verificationApiClient.reattribuer(session.getAccessToken(), selection.resume().id(), agentChoisi, motif),
                agentChoisi == null ? "Demande remise dans la file d'attente." : "Demande réattribuée.");
    }

    public void annuler() {
        executer(() -> verificationApiClient.annuler(session.getAccessToken(), selection.resume().id(), motif), "Demande annulée.");
    }

    public void revoquer() {
        executer(() -> verificationApiClient.revoquer(session.getAccessToken(), selection.resume().id(), motif),
                "Vérification révoquée : le badge est retiré.");
    }

    private void executer(java.util.function.Supplier<VerificationDetailResponse> action, String succes) {
        try {
            selection = action.get();
            motif = null;
            agentChoisi = null;
            rechargerListes();
            message(FacesMessage.SEVERITY_INFO, succes);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public void telecharger(Long documentId) {
        try {
            TelechargementJustificatif.envoyer(
                    verificationApiClient.telecharger(session.getAccessToken(), selection.resume().id(), documentId));
        } catch (ApiException | IOException e) {
            message(FacesMessage.SEVERITY_ERROR, "Téléchargement impossible : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ agents

    public void ajouterAgent() {
        try {
            agents = verificationApiClient.ajouterAgent(session.getAccessToken(), emailNouvelAgent);
            message(FacesMessage.SEVERITY_INFO, "Rôle d'agent donné à " + emailNouvelAgent
                    + ". Il s'applique à sa prochaine connexion, ou au plus tard dans 15 minutes.");
            emailNouvelAgent = null;
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public void retirerAgent(Long utilisateurId) {
        try {
            agents = verificationApiClient.retirerAgent(session.getAccessToken(), utilisateurId);
            message(FacesMessage.SEVERITY_INFO, "Rôle d'agent retiré.");
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(gravite, texte, null));
    }

    // ------------------------------------------------------------------ lecture

    public List<String> getStatutsFiltre() {
        return List.of("EN_ATTENTE", "EN_COURS", "A_COMPLETER", "APPROUVEE", "REFUSEE", "ANNULEE", "REVOQUEE");
    }

    public boolean isReattribuable() {
        return selection != null && OUVERTES_REATTRIBUABLES.contains(selection.resume().statut());
    }

    public boolean isAnnulable() {
        return selection != null && OUVERTES.contains(selection.resume().statut());
    }

    public boolean isRevocable() {
        return selection != null && "APPROUVEE".equals(selection.resume().statut());
    }

    public List<EvenementVerificationResponse> getHistorique() {
        if (selection == null) return List.of();
        List<EvenementVerificationResponse> liste = new ArrayList<>(selection.historique());
        Collections.reverse(liste);
        return liste;
    }

    public long compte(String statut) {
        if (statistiques == null || statistiques.parStatut() == null) return 0;
        Long n = statistiques.parStatut().get(statut);
        return n == null ? 0 : n;
    }

    public String getDelaiMoyen() {
        if (statistiques == null || statistiques.delaiMoyenDecisionHeures() == null) return "—";
        double h = statistiques.delaiMoyenDecisionHeures();
        return h < 48 ? String.format(java.util.Locale.FRANCE, "%.1f h", h) : String.format(java.util.Locale.FRANCE, "%.1f jours", h / 24);
    }

    public String getFiltreStatut() { return filtreStatut; }
    public List<VerificationResumeResponse> getDemandes() { return demandes; }
    public List<AgentVerificationResponse> getAgents() { return agents; }
    public VerificationDetailResponse getSelection() { return selection; }
    public String getErreur() { return erreur; }
    public Long getAgentChoisi() { return agentChoisi; }
    public void setAgentChoisi(Long agentChoisi) { this.agentChoisi = agentChoisi; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getEmailNouvelAgent() { return emailNouvelAgent; }
    public void setEmailNouvelAgent(String emailNouvelAgent) { this.emailNouvelAgent = emailNouvelAgent; }
}
