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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import sn.ucad.nexora.web.client.VerificationApiClient;
import sn.ucad.nexora.web.dto.verification.ControleResponse;
import sn.ucad.nexora.web.dto.verification.DocumentVerificationResponse;
import sn.ucad.nexora.web.dto.verification.EvenementVerificationResponse;
import sn.ucad.nexora.web.dto.verification.VerificationDetailResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;
import sn.ucad.nexora.web.util.TelechargementJustificatif;

/**
 * Examen d'une demande par l'agent ({@code verifications/examen.xhtml?id=}) : prise en charge,
 * documents, points de contrôle, décision motivée. Toutes les règles (agent assigné, motifs,
 * points bloquants) sont appliquées par espace-service ; la page affiche ses refus tels quels.
 */
@Named
@ViewScoped
public class ExamenVerificationBean implements Serializable {

    @Inject
    private transient VerificationApiClient verificationApiClient;

    @Inject
    private SessionBean session;

    private Long id;
    private VerificationDetailResponse demande;
    private String erreur;

    /** Saisies de la page : résultat et commentaire par point de contrôle, motif de rejet par document. */
    private final Map<String, String> resultats = new HashMap<>();
    private final Map<String, String> commentaires = new HashMap<>();
    private final Map<Long, String> motifsDocuments = new HashMap<>();
    private String motifDecision;

    @PostConstruct
    public void charger() {
        String brut = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        try {
            id = Long.valueOf(brut);
        } catch (NumberFormatException e) {
            erreur = "Demande introuvable";
            return;
        }
        try {
            appliquer(verificationApiClient.detailAgent(session.getAccessToken(), id));
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    private void appliquer(VerificationDetailResponse detail) {
        demande = detail;
        resultats.clear();
        commentaires.clear();
        for (ControleResponse c : detail.controles()) {
            resultats.put(c.code(), c.resultat());
            commentaires.put(c.code(), c.commentaire());
        }
    }

    // ------------------------------------------------------------------ actions

    public void prendre() {
        executer(() -> verificationApiClient.prendre(session.getAccessToken(), id), "Demande prise en charge.");
    }

    public void accepterDocument(Long documentId) {
        executer(() -> verificationApiClient.examinerDocument(session.getAccessToken(), id, documentId, "ACCEPTE", null),
                "Document accepté.");
    }

    public void rejeterDocument(Long documentId) {
        executer(() -> verificationApiClient.examinerDocument(session.getAccessToken(), id, documentId, "REJETE",
                motifsDocuments.get(documentId)), "Document rejeté.");
    }

    /** Enregistre les points de contrôle modifiés sur la page (et seulement ceux-là). */
    public void enregistrerControles() {
        // Chaque appel renvoie la demande à jour et appliquer() réinitialise les saisies : on fige
        // d'abord ce que l'agent a choisi sur la page.
        Map<String, String> resultatsSaisis = new HashMap<>(resultats);
        Map<String, String> commentairesSaisis = new HashMap<>(commentaires);
        int enregistres = 0;
        for (ControleResponse c : List.copyOf(demande.controles())) {
            String resultat = resultatsSaisis.get(c.code());
            String commentaire = vide(commentairesSaisis.get(c.code())) ? null : commentairesSaisis.get(c.code()).trim();
            if (Objects.equals(resultat, c.resultat()) && Objects.equals(commentaire, c.commentaire())) continue;
            try {
                appliquer(verificationApiClient.controler(session.getAccessToken(), id, c.code(), resultat, commentaire));
                enregistres++;
            } catch (ApiException e) {
                // Les saisies non enregistrées restent affichées pour que l'agent les corrige.
                resultats.putAll(resultatsSaisis);
                commentaires.putAll(commentairesSaisis);
                message(FacesMessage.SEVERITY_ERROR, c.libelle() + " : " + e.getMessage());
                return;
            }
        }
        message(FacesMessage.SEVERITY_INFO, enregistres == 0 ? "Aucun changement." : "Contrôles enregistrés.");
    }

    public void demanderInformations() {
        executer(() -> verificationApiClient.demanderInformations(session.getAccessToken(), id, motifDecision),
                "Informations demandées au professionnel.");
    }

    public void refuser() {
        executer(() -> verificationApiClient.refuser(session.getAccessToken(), id, motifDecision), "Vérification refusée.");
    }

    public void approuver() {
        executer(() -> verificationApiClient.approuver(session.getAccessToken(), id), "Espace vérifié.");
    }

    public void telecharger(Long documentId) {
        try {
            TelechargementJustificatif.envoyer(verificationApiClient.telecharger(session.getAccessToken(), id, documentId));
        } catch (ApiException | IOException e) {
            message(FacesMessage.SEVERITY_ERROR, "Téléchargement impossible : " + e.getMessage());
        }
    }

    private void executer(java.util.function.Supplier<VerificationDetailResponse> action, String succes) {
        try {
            appliquer(action.get());
            motifDecision = null;
            message(FacesMessage.SEVERITY_INFO, succes);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(gravite, texte, null));
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }

    // ------------------------------------------------------------------ lecture pour la page

    public VerificationDetailResponse getDemande() {
        return demande;
    }

    public String getErreur() {
        return erreur;
    }

    public String getStatut() {
        return demande == null ? null : demande.resume().statut();
    }

    /** Demande dans la file commune : l'agent peut la prendre. */
    public boolean isPrenable() {
        return "EN_ATTENTE".equals(getStatut());
    }

    /** Demande en cours d'examen par cet agent (espace-service ne la montre en EN_COURS qu'à lui). */
    public boolean isModifiable() {
        return "EN_COURS".equals(getStatut());
    }

    /** Documents à afficher : les remplacés sont masqués (ils restent dans l'historique). */
    public List<DocumentVerificationResponse> getDocuments() {
        return demande == null ? List.of() : demande.documents().stream().filter(d -> !"REMPLACE".equals(d.statut())).toList();
    }

    public List<EvenementVerificationResponse> getHistorique() {
        if (demande == null) return List.of();
        List<EvenementVerificationResponse> liste = new ArrayList<>(demande.historique());
        Collections.reverse(liste);
        return liste;
    }

    public Map<String, String> getResultats() {
        return resultats;
    }

    public Map<String, String> getCommentaires() {
        return commentaires;
    }

    public Map<Long, String> getMotifsDocuments() {
        return motifsDocuments;
    }

    public String getMotifDecision() {
        return motifDecision;
    }

    public void setMotifDecision(String motifDecision) {
        this.motifDecision = motifDecision;
    }
}
