package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.dto.auth.AuthenticationResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code connexion.xhtml}. */
@Named
@ViewScoped
public class ConnexionBean implements Serializable {

    @Inject
    private transient AuthApiClient authApiClient;

    @Inject
    private SessionBean session;

    private String email;
    private String motDePasse;

    /** Vue demandée avant la redirection vers la connexion, ex. par {@code SessionBean.exigerConnexion()}. */
    private String redirect;

    public String connecter() {
        try {
            AuthenticationResponse reponse = authApiClient.connecter(email, motDePasse);
            session.connecter(reponse.accessToken(), reponse.refreshToken(), reponse.account());
            return destination(redirect);
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Connexion impossible", e.getMessage()));
            return null;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    /**
     * Où aller après la connexion : la page demandée, paramètres compris (« /signaler.xhtml?espace=4 »).
     * Uniquement une vue de Nexora : un chemin absolu local, jamais une adresse externe.
     */
    static String destination(String redirect) {
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/") || redirect.startsWith("//")
                || redirect.contains(":") || redirect.contains("\\")) {
            return "index?faces-redirect=true";
        }
        String[] parties = redirect.split("\\?", 2);
        String parametres = parties.length > 1 && !parties[1].isBlank() ? parties[1] + "&" : "";
        return parties[0].replaceFirst("\\.xhtml$", "") + "?" + parametres + "faces-redirect=true";
    }
}
