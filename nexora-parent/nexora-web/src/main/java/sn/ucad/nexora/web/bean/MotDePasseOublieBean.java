package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.error.ApiException;

/** Backing bean de {@code mot-de-passe-oublie.xhtml}. */
@Named
@ViewScoped
public class MotDePasseOublieBean implements Serializable {

    @Inject
    private transient AuthApiClient authApiClient;

    private String email;
    private boolean envoye;

    public String envoyer() {
        try {
            authApiClient.motDePasseOublie(email);
            envoye = true;
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Envoi impossible", e.getMessage()));
        }
        return null;
    }

    public boolean isEnvoye() {
        return envoye;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
