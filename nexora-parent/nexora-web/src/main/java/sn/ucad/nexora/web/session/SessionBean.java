package sn.ucad.nexora.web.session;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
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
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Le jeton d'accès est renouvelé quand il lui reste moins d'une minute. */
    private static final Duration MARGE_RENOUVELLEMENT = Duration.ofMinutes(1);

    @Inject
    private transient AuthApiClient authApiClient;

    private String accessToken;
    private String refreshToken;
    private AccountResponse compte;
    private Instant expirationJeton;

    public synchronized void connecter(String accessToken, String refreshToken, AccountResponse compte) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.compte = compte;
        this.expirationJeton = expirationDe(accessToken);
    }

    /**
     * Recharge le jeton d'accès — et avec lui les rôles du compte — depuis auth-service.
     * Appelé automatiquement avant l'expiration du jeton, et après une action qui change les
     * rôles (création ou suppression d'un espace) pour que la navigation les voie aussitôt.
     */
    public synchronized boolean rafraichir() {
        if (refreshToken == null) return false;
        try {
            AuthenticationResponse reponse = authApiClient.rafraichir(refreshToken);
            connecter(reponse.accessToken(), reponse.refreshToken(), reponse.account());
            return true;
        } catch (ApiException e) {
            LOG.warn("Rafraîchissement de la session impossible : {}", e.getMessage());
            return false;
        }
    }

    /**
     * Le jeton d'accès ne vit que 15 minutes (auth-service) : on le renouvelle à l'approche de son
     * expiration. S'il a expiré sans pouvoir l'être (refresh token expiré ou révoqué), la session est
     * fermée — sinon le menu afficherait un compte connecté dont tous les appels échouent en 403.
     */
    private void verifierJeton() {
        if (accessToken == null || expirationJeton == null
                || Instant.now().plus(MARGE_RENOUVELLEMENT).isBefore(expirationJeton)) {
            return;
        }
        if (!rafraichir() && !Instant.now().isBefore(expirationJeton)) {
            LOG.info("Jeton d'accès expiré et non renouvelable : fin de session.");
            deconnecter();
        }
    }

    /** Date d'expiration lue dans le jeton (claim {@code exp}) ; la signature est vérifiée par les services, pas ici. */
    private static Instant expirationDe(String jwt) {
        if (jwt == null) return null;
        try {
            String charge = jwt.split("\\.")[1];
            long exp = JSON.readTree(Base64.getUrlDecoder().decode(charge)).path("exp").asLong(0);
            return exp > 0 ? Instant.ofEpochSecond(exp) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Vrai si le compte possède au moins un espace professionnel (rôle FOURNISSEUR). */
    public boolean isFournisseur() {
        return aLeRole("FOURNISSEUR");
    }

    /** Agent de vérification des espaces (docs/architecture-acteurs.md §8). */
    public boolean isAgentVerification() {
        return aLeRole("AGENT_VERIFICATION");
    }

    /** Administrateur (ADMIN ou SUPER_ADMIN) : supervision de la vérification. */
    public boolean isAdministrateur() {
        return aLeRole("ADMIN") || aLeRole("SUPER_ADMIN");
    }

    private boolean aLeRole(String code) {
        return compte != null && compte.roles() != null && compte.roles().contains(code);
    }

    public synchronized void deconnecter() {
        this.accessToken = null;
        this.refreshToken = null;
        this.compte = null;
        this.expirationJeton = null;
    }

    /** Action de navigation pour le lien « Déconnexion » de la barre de navigation. */
    public String deconnecterEtRediriger() {
        deconnecter();
        return "index?faces-redirect=true";
    }

    public synchronized boolean isConnecte() {
        verifierJeton();
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

    /** Garde des pages de l'agent de vérification : connexion puis rôle AGENT_VERIFICATION. */
    public void exigerAgentVerification() {
        exigerRole(isConnecte() && isAgentVerification());
    }

    /** Garde des pages d'administration : connexion puis rôle ADMIN ou SUPER_ADMIN. */
    public void exigerAdministrateur() {
        exigerRole(isConnecte() && isAdministrateur());
    }

    /**
     * Visiteur non connecté : vers la connexion (comme {@link #exigerConnexion()}). Compte connecté
     * sans le rôle : vers l'accueil — la page n'existe pas pour lui, pas même son contenu vide.
     */
    private void exigerRole(boolean autorise) {
        if (autorise) return;
        if (!isConnecte()) {
            exigerConnexion();
            return;
        }
        FacesContext contexte = FacesContext.getCurrentInstance();
        ExternalContext externe = contexte.getExternalContext();
        try {
            externe.redirect(externe.getRequestContextPath() + "/index.xhtml");
        } catch (IOException e) {
            throw new FacesException(e);
        }
        contexte.responseComplete();
    }

    public synchronized String getAccessToken() {
        verifierJeton();
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
