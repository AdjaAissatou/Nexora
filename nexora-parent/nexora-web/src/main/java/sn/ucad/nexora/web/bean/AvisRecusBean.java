package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sn.ucad.nexora.web.client.AvisApiClient;
import sn.ucad.nexora.web.dto.recherche.AvisDtos.AvisRecu;
import sn.ucad.nexora.web.dto.recherche.AvisDtos.AvisRecus;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Onglet « Avis reçus » de Mon espace (docs/architecture-acteurs.md §22) : synthèse des notes de
 * l'espace affiché, ses avis (sur lui et sur ses offres) et la réponse publique du professionnel.
 */
@Named
@ViewScoped
public class AvisRecusBean implements Serializable {

    private static final String FORMULAIRE = "avisRecusForm";

    @Inject
    private transient AvisApiClient avisApiClient;

    @Inject
    private SessionBean session;

    @Inject
    private MonEspaceBean monEspace;

    private AvisRecus recus;
    private boolean charge;
    private String erreur;

    /** Réponse en cours d'écriture, par avis. */
    private final Map<Long, String> brouillons = new HashMap<>();
    /** Avis dont on modifie la réponse déjà publiée. */
    private final Set<Long> enEdition = new HashSet<>();

    public AvisRecus getRecus() {
        if (!charge) charger();
        return recus;
    }

    private void charger() {
        charge = true;
        if (monEspace.getEspace() == null) return;
        try {
            recus = avisApiClient.recus(session.getAccessToken(), monEspace.getEspace().id());
            erreur = null;
        } catch (ApiException e) {
            recus = null;
            erreur = e.getMessage();
        }
    }

    public void repondre(Long avisId) {
        String texte = brouillons.get(avisId);
        if (texte == null || texte.isBlank()) {
            message(FacesMessage.SEVERITY_ERROR, "Écrivez votre réponse avant de la publier.");
            return;
        }
        try {
            boolean modification = enEdition.contains(avisId);
            avisApiClient.repondre(session.getAccessToken(), avisId, texte);
            brouillons.remove(avisId);
            enEdition.remove(avisId);
            charger();
            message(FacesMessage.SEVERITY_INFO, modification ? "Réponse modifiée." : "Réponse publiée : elle est visible sous l'avis, sur votre fiche.");
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public void modifierReponse(AvisRecu a) {
        enEdition.add(a.id());
        brouillons.put(a.id(), a.reponse());
    }

    public void annuler(Long avisId) {
        enEdition.remove(avisId);
        brouillons.remove(avisId);
    }

    public void retirerReponse(Long avisId) {
        try {
            avisApiClient.retirerReponse(session.getAccessToken(), avisId);
            enEdition.remove(avisId);
            brouillons.remove(avisId);
            charger();
            message(FacesMessage.SEVERITY_INFO, "Réponse retirée.");
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    /** Le champ de réponse s'affiche pour un avis sans réponse, ou dont on modifie la réponse. */
    public boolean formulaireVisible(AvisRecu a) {
        return !a.avecReponse() || enEdition.contains(a.id());
    }

    /** Nombre d'avis à {@code etoiles} étoiles (1 à 5). */
    public int nombre(int etoiles) {
        AvisRecus r = getRecus();
        return r == null || r.repartition() == null || r.repartition().size() < etoiles ? 0 : r.repartition().get(etoiles - 1);
    }

    /** Largeur de la barre de répartition, en pourcentage du total. */
    public int pourcentage(int etoiles) {
        AvisRecus r = getRecus();
        return r == null || r.nombre() == 0 ? 0 : Math.round(100f * nombre(etoiles) / r.nombre());
    }

    public int getSansReponse() {
        AvisRecus r = getRecus();
        return r == null ? 0 : r.sansReponse();
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(FORMULAIRE, new FacesMessage(gravite, texte, null));
    }

    public String getErreur() { return erreur; }
    public Map<Long, String> getBrouillons() { return brouillons; }
}
