package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.UserApiClient;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.user.UpdateUserRequest;
import sn.ucad.nexora.web.dto.user.UserResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

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
    private SessionBean session;

    private UserResponse profil;
    private List<EspaceResponse> mesEspaces;
    private String erreur;

    private String prenom;
    private String nom;
    private String telephone;

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
