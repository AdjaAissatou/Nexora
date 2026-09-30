package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sn.ucad.nexora.web.client.AvisApiClient;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.RechercheApiClient;
import sn.ucad.nexora.web.dto.catalogue.OffreSummaryResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.recherche.AvisResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code espace.xhtml} — fiche publique d'un espace professionnel. */
@Named
@ViewScoped
public class EspaceDetailBean implements Serializable {

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private transient AvisApiClient avisApiClient;

    @Inject
    private transient RechercheApiClient rechercheApiClient;

    @Inject
    private transient sn.ucad.nexora.web.client.UserApiClient userApiClient;

    @Inject
    private SessionBean session;

    /** Utilisateur connecté (id du profil) : pour savoir s'il est le propriétaire ou s'il a déjà noté. */
    private Long monUtilisateurId;
    /** Mon avis sur cet espace, même masqué (null si je n'en ai pas). */
    private AvisApiClient.MonAvis monAvis;
    /** Horaires et état « ouvert maintenant » (§10) ; null si indisponibles. */
    private sn.ucad.nexora.web.dto.espace.HorairesDtos.HorairesResponse horaires;
    private Integer nouvelleNote;
    private String nouveauCommentaire;

    private Long id;
    private EspaceResponse espace;
    private List<OffreSummaryResponse> offres;
    private List<AvisResponse> avis;
    private String erreur;
    private boolean trouve;
    private boolean favori;

    public void charger() {
        if (id == null) return;
        try {
            espace = espaceApiClient.obtenir(id);
            trouve = true;
            espaceApiClient.enregistrerVue(id);
        } catch (ApiException e) {
            erreur = e.getMessage();
            trouve = false;
            return;
        }
        try {
            offres = catalogueApiClient.rechercher(CritereRecherche.parEspace(id)).contenu();
        } catch (ApiException e) {
            offres = List.of();
        }
        try {
            horaires = espaceApiClient.horaires(session.isConnecte() ? session.getAccessToken() : null, id);
        } catch (ApiException e) {
            horaires = null;
        }
        try {
            avis = avisApiClient.parEspace(id);
        } catch (ApiException e) {
            avis = List.of();
        }
        if (session.isConnecte()) {
            try {
                monUtilisateurId = userApiClient.obtenir(session.getAccessToken(), session.getCompte().id()).id();
            } catch (ApiException e) {
                monUtilisateurId = null;
            }
            chargerMonAvis();
            rechercheApiClient.enregistrerConsultation(session.getAccessToken(), null, id);
            try {
                favori = rechercheApiClient.listerFavoris(session.getAccessToken()).stream()
                        .anyMatch(f -> id.equals(f.espaceId()));
            } catch (ApiException e) {
                favori = false;
            }
        }
    }

    public void basculerFavori() {
        if (!session.isConnecte() || espace == null) return;
        try {
            if (favori) {
                rechercheApiClient.supprimerFavori(session.getAccessToken(), null, id);
            } else {
                rechercheApiClient.ajouterFavori(session.getAccessToken(), null, id);
            }
            favori = !favori;
        } catch (ApiException e) {
            // Silencieux : un aller-retour favori raté n'empêche pas de consulter la fiche.
        }
    }

    /** Publie l'avis du visiteur connecté ; la note de l'espace est recalculée par recherche-service. */
    public void publierAvis() {
        if (!isPeutDonnerAvis()) return;
        if (nouvelleNote == null || nouvelleNote < 1 || nouvelleNote > 5) {
            message(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Choisissez une note de 1 à 5 étoiles.");
            return;
        }
        try {
            avisApiClient.publier(session.getAccessToken(), id, nouvelleNote, nouveauCommentaire);
            avis = avisApiClient.parEspace(id);
            espace = espaceApiClient.obtenir(id);
            chargerMonAvis();
            nouvelleNote = null;
            nouveauCommentaire = null;
            message(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Merci ! Votre avis est publié.");
        } catch (ApiException e) {
            message(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private static void message(jakarta.faces.application.FacesMessage.Severity gravite, String texte) {
        jakarta.faces.context.FacesContext.getCurrentInstance()
                .addMessage("avisForm", new jakarta.faces.application.FacesMessage(gravite, texte, null));
    }

    public boolean isProprietaire() {
        return monUtilisateurId != null && espace != null && monUtilisateurId.equals(espace.utilisateurId());
    }

    private void chargerMonAvis() {
        try {
            monAvis = monUtilisateurId == null ? null : avisApiClient.mien(session.getAccessToken(), id);
        } catch (ApiException e) {
            monAvis = null;
        }
    }

    /** Un avis par personne et par espace, y compris s'il a été masqué : on ne contourne pas la modération. */
    public boolean isDejaNote() {
        return monAvis != null;
    }

    public AvisApiClient.MonAvis getMonAvis() { return monAvis; }

    public boolean isPeutDonnerAvis() {
        return session.isConnecte() && monUtilisateurId != null && !isProprietaire() && !isDejaNote();
    }

    /** On ne signale ni son propre espace ni son propre avis. */
    public boolean estMonAvis(AvisResponse a) {
        return monUtilisateurId != null && monUtilisateurId.equals(a.utilisateurId());
    }

    public Integer getNouvelleNote() { return nouvelleNote; }
    public void setNouvelleNote(Integer nouvelleNote) { this.nouvelleNote = nouvelleNote; }
    public String getNouveauCommentaire() { return nouveauCommentaire; }
    public void setNouveauCommentaire(String nouveauCommentaire) { this.nouveauCommentaire = nouveauCommentaire; }

    public sn.ucad.nexora.web.dto.espace.HorairesDtos.HorairesResponse getHoraires() { return horaires; }

    public boolean isFavori() { return favori; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EspaceResponse getEspace() { return espace; }
    public List<OffreSummaryResponse> getOffres() { return offres; }
    public List<AvisResponse> getAvis() { return avis; }
    public String getErreur() { return erreur; }
    public boolean isTrouve() { return trouve; }
}
