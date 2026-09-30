package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import sn.ucad.nexora.web.client.VerificationApiClient;
import sn.ucad.nexora.web.dto.verification.VerificationResumeResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Files de travail de l'agent de vérification ({@code verifications/index.xhtml}) : « À traiter »
 * (file commune) puis ses propres demandes par statut. docs/architecture-acteurs.md §8.8.
 */
@Named
@ViewScoped
public class FileVerificationBean implements Serializable {

    /** Onglets de l'agent, dans l'ordre d'affichage : code de statut → libellé. */
    public record Onglet(String statut, String libelle) implements Serializable {}

    private static final List<Onglet> ONGLETS = List.of(
            new Onglet("EN_ATTENTE", "À traiter"),
            new Onglet("EN_COURS", "En cours"),
            new Onglet("A_COMPLETER", "Informations demandées"),
            new Onglet("APPROUVEE", "Approuvées"),
            new Onglet("REFUSEE", "Refusées"));

    private static final Set<String> STATUTS = Set.of("EN_ATTENTE", "EN_COURS", "A_COMPLETER", "APPROUVEE", "REFUSEE");

    @Inject
    private transient VerificationApiClient verificationApiClient;

    @Inject
    private SessionBean session;

    private String statut = "EN_ATTENTE";
    private List<VerificationResumeResponse> demandes = List.of();
    private String erreur;

    @PostConstruct
    public void charger() {
        String demande = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("statut");
        if (demande != null && STATUTS.contains(demande)) statut = demande;
        try {
            demandes = verificationApiClient.file(session.getAccessToken(), statut);
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public List<Onglet> getOnglets() {
        return ONGLETS;
    }

    public String getStatut() {
        return statut;
    }

    public String getLibelleOnglet() {
        return ONGLETS.stream().filter(o -> o.statut().equals(statut)).map(Onglet::libelle).findFirst().orElse("");
    }

    public List<VerificationResumeResponse> getDemandes() {
        return demandes;
    }

    public String getErreur() {
        return erreur;
    }
}
