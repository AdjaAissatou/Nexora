package com.nexora.web.bean;

import com.nexora.domain.user.Utilisateur;
import com.nexora.dto.EspaceViewDTO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Etat de session de l'utilisateur (JSF / MVC2). Ne contient aucune donnee
 * simulee : l'identite provient de {@code AuthService} et l'espace de
 * {@code EspaceService} (persistance PostgreSQL).
 *
 * <p>Navigation libre ; un compte n'est requis que pour commander / reserver /
 * creer un espace. Un utilisateur possede au plus un espace (dans cette IHM).</p>
 */
@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUtilisateur;
    private String nom;
    private boolean admin;

    private Long espaceId;
    private String espaceNom;
    private String espaceVille;

    // ------------------------------------------------------------------ Auth
    public boolean isConnecte()  { return idUtilisateur != null; }
    public boolean isAdmin()     { return admin; }
    public Long getIdUtilisateur() { return idUtilisateur; }
    public String getNom()       { return nom; }

    public String getInitiale() {
        String base = (nom != null && !nom.isBlank()) ? nom : "U";
        return base.substring(0, 1).toUpperCase();
    }

    /** Ouvre la session a partir de l'utilisateur authentifie/inscrit. */
    public void connecter(Utilisateur u) {
        this.idUtilisateur = u.getIdUtilisateur();
        String prenom = u.getPrenom();
        this.nom = (prenom != null && !prenom.isBlank()) ? prenom : u.getNom();
        if (this.nom == null || this.nom.isBlank()) this.nom = u.getEmail();
        this.admin = u.getProfile() != null
                && u.getProfile().getLibelle() != null
                && u.getProfile().getLibelle().toUpperCase().contains("ADMIN");
    }

    public String deconnecter() {
        this.idUtilisateur = null;
        this.nom = null;
        this.admin = false;
        this.espaceId = null;
        this.espaceNom = null;
        this.espaceVille = null;
        return "accueil?faces-redirect=true";
    }

    // -------------------------------------------------------------- Espace pro
    public boolean isPossedeEspace() { return espaceId != null; }
    public Long getEspaceId()        { return espaceId; }
    public String getEspaceNom()     { return espaceNom; }
    public String getEspaceVille()   { return espaceVille; }

    /** Memorise l'espace de l'utilisateur (apres creation ou chargement). */
    public void setEspace(EspaceViewDTO e, String ville) {
        this.espaceId = e.getId();
        this.espaceNom = e.getNomCommercial();
        this.espaceVille = ville;
    }
}
