package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.ucad.nexora.web.client.VerificationApiClient;
import sn.ucad.nexora.web.dto.verification.DocumentVerificationResponse;
import sn.ucad.nexora.web.dto.verification.EtatVerificationResponse;
import sn.ucad.nexora.web.dto.verification.EvenementVerificationResponse;
import sn.ucad.nexora.web.dto.verification.JustificatifAttendu;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;
import sn.ucad.nexora.web.util.TelechargementJustificatif;

/**
 * Onglet « Vérification » de {@code mon-espace.xhtml} (docs/architecture-acteurs.md §8.8) :
 * complétude du dossier, dépôt des justificatifs, envoi, suivi et résultat. L'espace concerné est
 * fixé par {@link MonEspaceBean} au chargement de la page.
 */
@Named
@ViewScoped
public class VerificationEspaceBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationEspaceBean.class);

    /** Statuts dans lesquels le dossier de la demande courante se modifie encore. */
    private static final Set<String> DOSSIER_MODIFIABLE = Set.of("BROUILLON", "A_COMPLETER");

    @Inject
    private transient VerificationApiClient verificationApiClient;

    @Inject
    private SessionBean session;

    private Long espaceId;
    private EtatVerificationResponse etat;
    private String erreur;

    /** Appelé par MonEspaceBean une fois l'espace sélectionné connu. */
    public void initialiser(Long espaceId) {
        this.espaceId = espaceId;
        recharger();
    }

    public void recharger() {
        if (espaceId == null) return;
        try {
            etat = verificationApiClient.etat(session.getAccessToken(), espaceId);
            erreur = null;
        } catch (ApiException e) {
            LOG.warn("État de vérification de l'espace {} indisponible : {}", espaceId, e.getMessage());
            etat = null;
            erreur = e.getMessage();
        }
    }

    // ------------------------------------------------------------------ actions

    /** Dépôt d'un justificatif : le type est porté par l'attribut {@code typeJustificatifId} du composant. */
    public void deposer(FileUploadEvent event) {
        Object type = event.getComponent().getAttributes().get("typeJustificatifId");
        Long typeId = type instanceof Number n ? n.longValue() : type == null ? null : Long.valueOf(type.toString());
        try {
            etat = verificationApiClient.deposerDocument(session.getAccessToken(), espaceId, typeId,
                    event.getFile().getFileName(), event.getFile().getContent());
            message(FacesMessage.SEVERITY_INFO, "Document ajouté : " + event.getFile().getFileName(), null);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, "Envoi impossible", e.getMessage());
        }
    }

    public void retirerDocument(Long documentId) {
        try {
            etat = verificationApiClient.retirerDocument(session.getAccessToken(), espaceId, documentId);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, "Retrait impossible", e.getMessage());
        }
    }

    public void soumettre() {
        try {
            etat = verificationApiClient.soumettre(session.getAccessToken(), espaceId);
            message(FacesMessage.SEVERITY_INFO, "Votre demande de vérification a bien été envoyée.", null);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, "Envoi impossible", e.getMessage());
        }
    }

    public void retirerDemande() {
        try {
            etat = verificationApiClient.retirer(session.getAccessToken(), espaceId);
            message(FacesMessage.SEVERITY_INFO, "Votre demande de vérification a été retirée.", null);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, "Retrait impossible", e.getMessage());
        }
    }

    public void telecharger(Long documentId) {
        if (etat == null || etat.demande() == null) return;
        try {
            TelechargementJustificatif.envoyer(verificationApiClient.telecharger(
                    session.getAccessToken(), etat.demande().resume().id(), documentId));
        } catch (ApiException | IOException e) {
            message(FacesMessage.SEVERITY_ERROR, "Téléchargement impossible", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ lecture pour la page

    public EtatVerificationResponse getEtat() {
        return etat;
    }

    public String getErreur() {
        return erreur;
    }

    public String getCodeEtat() {
        return etat == null ? null : etat.etat();
    }

    /** Vrai si la dernière demande est encore en préparation ou à compléter. */
    public boolean isDossierModifiable() {
        return etat != null && etat.demande() != null && DOSSIER_MODIFIABLE.contains(etat.demande().resume().statut());
    }

    /** Justificatifs déposés d'un type, dans le dossier en cours (les documents remplacés sont masqués). */
    public List<DocumentVerificationResponse> documentsDuType(Long typeJustificatifId) {
        if (!isDossierModifiable()) return List.of();
        return etat.demande().documents().stream()
                .filter(d -> d.typeJustificatifId().equals(typeJustificatifId) && !"REMPLACE".equals(d.statut()))
                .toList();
    }

    /** Documents de la dernière demande, hors remplacés : ce que l'agent examine ou a examiné. */
    public List<DocumentVerificationResponse> getDocumentsEnvoyes() {
        if (etat == null || etat.demande() == null) return List.of();
        return etat.demande().documents().stream().filter(d -> !"REMPLACE".equals(d.statut())).toList();
    }

    /**
     * Marque d'une ligne de justificatif : « ok » s'il est fourni, « manque » s'il est obligatoire et
     * qu'aucun document de son groupe « au choix » n'a été fourni, sinon rien (facultatif, ou groupe
     * déjà satisfait par un autre document).
     */
    public String marque(JustificatifAttendu j) {
        if (j.fourni()) return "ok";
        if (!j.obligatoire()) return "";
        boolean groupeSatisfait = j.groupeAlternatif() != null && etat.justificatifsAttendus().stream()
                .anyMatch(autre -> j.groupeAlternatif().equals(autre.groupeAlternatif()) && autre.fourni());
        return groupeSatisfait ? "" : "manque";
    }

    public List<JustificatifAttendu> getJustificatifsObligatoires() {
        return etat == null ? List.of() : etat.justificatifsAttendus().stream().filter(JustificatifAttendu::obligatoire).toList();
    }

    public List<JustificatifAttendu> getJustificatifsFacultatifs() {
        return etat == null ? List.of() : etat.justificatifsAttendus().stream().filter(j -> !j.obligatoire()).toList();
    }

    /** Historique de la dernière demande, du plus récent au plus ancien. */
    public List<EvenementVerificationResponse> getHistorique() {
        if (etat == null || etat.demande() == null) return List.of();
        List<EvenementVerificationResponse> liste = new java.util.ArrayList<>(etat.demande().historique());
        java.util.Collections.reverse(liste);
        return liste;
    }

    public String getLibelleEnvoi() {
        return etat != null && etat.demande() != null && "A_COMPLETER".equals(etat.demande().resume().statut())
                ? "Renvoyer mon dossier" : "Demander la vérification";
    }

    private static void message(FacesMessage.Severity gravite, String resume, String detail) {
        FacesContext.getCurrentInstance().addMessage("verificationForm", sn.ucad.nexora.web.util.Messages.complet(gravite, resume, detail));
    }
}
