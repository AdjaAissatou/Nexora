package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.RechercheApiClient;
import sn.ucad.nexora.web.client.UserApiClient;
import sn.ucad.nexora.web.dto.catalogue.OffreDetailResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.recherche.FavoriResponse;
import sn.ucad.nexora.web.dto.recherche.HistoriqueConsultationResponse;
import sn.ucad.nexora.web.dto.user.UpdateUserRequest;
import sn.ucad.nexora.web.dto.user.UserResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;
import sn.ucad.nexora.web.util.FormatBean;

/**
 * Backing bean de {@code mon-compte.xhtml} — profil personnel, commun à tout compte
 * connecté (client ou professionnel). La gestion d'un espace professionnel lui-même
 * vit dans {@link MonEspaceBean} ; les deux se renvoient l'une vers l'autre
 * (cf. docs/architecture-acteurs.md §5).
 */
@Named
@ViewScoped
public class MonCompteBean implements Serializable {

    @Inject
    private transient UserApiClient userApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private transient RechercheApiClient rechercheApiClient;

    @Inject
    private SessionBean session;

    @Inject
    private transient FormatBean format;

    private UserResponse profil;
    private List<EspaceResponse> mesEspaces;
    private String erreur;

    private String prenom;
    private String nom;
    private String telephone;

    private List<Element> favoris = List.of();
    private List<Element> historique = List.of();

    /** Élément affichable dans "Mes favoris" / "Mon historique" — une offre ou un espace. */
    public record Element(Long offreId, Long espaceId, String titre, String sousTitre, String image, String href)
            implements Serializable {}

    @PostConstruct
    public void charger() {
        try {
            profil = userApiClient.obtenir(session.getAccessToken(), session.getCompte().id());
            prenom = profil.firstName();
            nom = profil.lastName();
            telephone = profil.phone();
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        try {
            mesEspaces = espaceApiClient.mesEspaces(session.getAccessToken());
        } catch (ApiException e) {
            mesEspaces = List.of();
        }
        chargerFavoris();
        chargerHistorique();
    }

    private void chargerFavoris() {
        try {
            List<FavoriResponse> brut = rechercheApiClient.listerFavoris(session.getAccessToken());
            List<Element> elements = new ArrayList<>();
            for (FavoriResponse f : brut) {
                Element e = versElement(f.offreId(), f.espaceId());
                if (e != null) elements.add(e);
            }
            favoris = elements;
        } catch (ApiException e) {
            favoris = List.of();
        }
    }

    private void chargerHistorique() {
        try {
            List<HistoriqueConsultationResponse> brut = rechercheApiClient.listerHistorique(session.getAccessToken());
            // Chaque visite crée une ligne : on ne garde que la plus récente par offre/espace
            // (la liste arrive déjà triée du plus récent au plus ancien).
            Set<String> dejaVus = new HashSet<>();
            List<Element> elements = new ArrayList<>();
            for (HistoriqueConsultationResponse h : brut) {
                String cle = h.offreId() != null ? "O" + h.offreId() : "E" + h.espaceId();
                if (!dejaVus.add(cle)) continue;
                Element e = versElement(h.offreId(), h.espaceId());
                if (e != null) elements.add(e);
                if (elements.size() >= 20) break;
            }
            historique = elements;
        } catch (ApiException e) {
            historique = List.of();
        }
    }

    /** Une offre/espace supprimé(e) depuis reste référencé(e) en favori/historique : on l'ignore silencieusement. */
    private Element versElement(Long offreId, Long espaceId) {
        try {
            if (offreId != null) {
                OffreDetailResponse o = catalogueApiClient.obtenir(offreId);
                return new Element(offreId, null, o.titre(), format.prix(o.prix()), o.imagePrincipale(), "/offre.xhtml?id=" + offreId);
            }
            if (espaceId != null) {
                EspaceResponse esp = espaceApiClient.obtenir(espaceId);
                return new Element(null, espaceId, esp.nom(), esp.slogan(), esp.logo(), "/espace.xhtml?id=" + espaceId);
            }
        } catch (ApiException ignored) {
            // Offre/espace introuvable (supprimé) : on n'affiche pas cette ligne.
        }
        return null;
    }

    public void retirerFavori(Long offreId, Long espaceId) {
        try {
            rechercheApiClient.supprimerFavori(session.getAccessToken(), offreId, espaceId);
            chargerFavoris();
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Suppression impossible", e.getMessage()));
        }
    }

    public void effacerHistorique() {
        try {
            rechercheApiClient.effacerHistorique(session.getAccessToken());
            historique = List.of();
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Suppression impossible", e.getMessage()));
        }
    }

    public List<Element> getFavoris() {
        return favoris;
    }

    public List<Element> getHistorique() {
        return historique;
    }

    public String enregistrerProfil() {
        try {
            profil = userApiClient.mettreAJour(session.getAccessToken(), new UpdateUserRequest(prenom, nom, telephone));
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Profil mis à jour.", null));
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mise à jour impossible", e.getMessage()));
        }
        return null;
    }

    public boolean isPossedeEspace() {
        return mesEspaces != null && !mesEspaces.isEmpty();
    }

    public EspaceResponse getPremierEspace() {
        return mesEspaces == null || mesEspaces.isEmpty() ? null : mesEspaces.get(0);
    }

    public int getNombreEspacesSupplementaires() {
        return mesEspaces == null || mesEspaces.isEmpty() ? 0 : mesEspaces.size() - 1;
    }

    public UserResponse getProfil() {
        return profil;
    }

    public String getErreur() {
        return erreur;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
