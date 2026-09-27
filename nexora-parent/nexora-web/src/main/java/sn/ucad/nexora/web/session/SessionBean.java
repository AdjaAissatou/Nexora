package sn.ucad.nexora.web.session;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
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

    /**
     * Garde d'accès à placer dans le {@code f:metadata} de toute page réservée aux comptes
     * connectés : {@code <f:event type="preRenderView" listener="#{sessionBean.exigerConnexion}" />}.
     * Redirige vers la connexion (avec la page demandée en paramètre) si le visiteur n'est pas connecté.
     */
    public void exigerConnexion() {
        if (isConnecte()) {
            return;
        }
        FacesContext contexte = FacesContext.getCurrentInstance();
        ExternalContext externe = contexte.getExternalContext();
        String vue = URLEncoder.encode(contexte.getViewRoot().getViewId(), StandardCharsets.UTF_8);
        try {
            externe.redirect(externe.getRequestContextPath() + "/connexion.xhtml?redirect=" + vue);
        } catch (IOException e) {
            throw new FacesException(e);
        }
        contexte.responseComplete();
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
