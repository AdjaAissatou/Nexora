package sn.ucad.nexora.web.session;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.dto.auth.AccountResponse;

/**
 * Le *compte* connecté (auth-service), partagé par toutes les pages de la session HTTP.
 * Compte ≠ Utilisateur ≠ Espace professionnel : ce bean ne contient que l'identité d'authentification.
 */
@Named
@SessionScoped
public class SessionBean implements Serializable {

    private String accessToken;
    private String refreshToken;
    private AccountResponse compte;

    public void connecter(String accessToken, String refreshToken, AccountResponse compte) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.compte = compte;
    }

    public void deconnecter() {
        this.accessToken = null;
        this.refreshToken = null;
        this.compte = null;
    }

    /** Action de navigation pour le lien « Déconnexion » de la barre de navigation. */
    public String deconnecterEtRediriger() {
        deconnecter();
        return "index?faces-redirect=true";
    }

    public boolean isConnecte() {
        return accessToken != null && compte != null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AccountResponse getCompte() {
        return compte;
    }

    public String getPrenom() {
        return compte != null ? compte.firstName() : "";
    }

    public String getNomComplet() {
        return compte != null ? compte.nomComplet() : "";
    }
}
