package sn.ucad.nexora.web.session;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.dto.auth.AccountResponse;
import sn.ucad.nexora.web.dto.auth.AuthenticationResponse;
import sn.ucad.nexora.web.error.ApiException;

/**
 * Le *compte* connecté (auth-service), partagé par toutes les pages de la session HTTP.
 * Compte ≠ Utilisateur ≠ Espace professionnel : ce bean ne contient que l'identité d'authentification.
 */
@Named
@SessionScoped
public class SessionBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SessionBean.class);

    @Inject
    private transient AuthApiClient authApiClient;

    private String accessToken;
    private String refreshToken;
    private AccountResponse compte;

    public void connecter(String accessToken, String refreshToken, AccountResponse compte) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.compte = compte;
    }

    /**
     * Recharge le jeton d'accès — et avec lui les rôles du compte — depuis auth-service.
     * À appeler après une action qui change les rôles (création ou suppression d'un espace) :
     * sans cela, la navigation ne les verrait qu'à la prochaine connexion.
     */
    public void rafraichir() {
        if (refreshToken == null) return;
        try {
            AuthenticationResponse reponse = authApiClient.rafraichir(refreshToken);
            connecter(reponse.accessToken(), reponse.refreshToken(), reponse.account());
        } catch (ApiException e) {
            LOG.warn("Rafraîchissement de la session impossible : {}", e.getMessage());
        }
    }

    /** Vrai si le compte possède au moins un espace professionnel (rôle FOURNISSEUR). */
    public boolean isFournisseur() {
        return compte != null && compte.roles() != null && compte.roles().contains("FOURNISSEUR");
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
