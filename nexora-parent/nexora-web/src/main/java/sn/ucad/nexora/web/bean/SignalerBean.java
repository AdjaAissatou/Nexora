package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;
import sn.ucad.nexora.web.client.AvisApiClient;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.dto.recherche.AvisResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Signaler un espace, une offre ou un avis ({@code signaler.xhtml?espace=|offre=|avis=}), réservé aux
 * comptes connectés. Le signalement part dans la file de la modération (docs/architecture-acteurs.md §9.10).
 */
@Named
@ViewScoped
public class SignalerBean implements Serializable {

    @Inject
    private transient AvisApiClient avisApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private SessionBean session;

    private Long espace;
    private Long offre;
    private Long avis;

    /** Ce qui est signalé, en clair, et où revenir ensuite. */
    private String type;
    private String libelle;
    private String retour;
    private boolean trouve;
    private boolean envoye;

    private Map<String, String> motifs = Map.of();
    private String motif;
    private String description;

    public void charger() {
        if (!session.isConnecte()) return;
        try {
            motifs = avisApiClient.motifsSignalement();
            if (espace != null) {
                type = "cet espace";
                libelle = espaceApiClient.obtenir(espace).nom();
                retour = "/espace.xhtml?id=" + espace;
            } else if (offre != null) {
                type = "cette offre";
                libelle = catalogueApiClient.obtenir(offre).titre();
                retour = "/offre.xhtml?id=" + offre;
            } else if (avis != null) {
                AvisResponse a = avisApiClient.un(avis);
                type = "cet avis";
                libelle = a.auteur() + " : « " + (a.commentaire() == null || a.commentaire().isBlank() ? a.note() + " étoile(s)" : a.commentaire()) + " »";
                retour = a.espaceId() != null ? "/espace.xhtml?id=" + a.espaceId() : "/index.xhtml";
            }
            trouve = libelle != null;
        } catch (ApiException e) {
            trouve = false;
        }
    }

    public void envoyer() {
        try {
            avisApiClient.signaler(session.getAccessToken(), espace, offre, avis, motif, description);
            envoye = true;
        } catch (ApiException e) {
            FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public Long getEspace() { return espace; }
    public void setEspace(Long espace) { this.espace = espace; }
    public Long getOffre() { return offre; }
    public void setOffre(Long offre) { this.offre = offre; }
    public Long getAvis() { return avis; }
    public void setAvis(Long avis) { this.avis = avis; }
    public String getType() { return type; }
    public String getLibelle() { return libelle; }
    public String getRetour() { return retour; }
    public boolean isTrouve() { return trouve; }
    public boolean isEnvoye() { return envoye; }
    public Map<String, String> getMotifs() { return motifs; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
