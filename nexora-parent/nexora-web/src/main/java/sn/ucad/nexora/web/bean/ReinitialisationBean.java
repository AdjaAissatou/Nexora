package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.error.ApiException;

/** Backing bean de {@code reinitialisation.xhtml} — saisie du code OTP + nouveau mot de passe. */
@Named
@ViewScoped
public class ReinitialisationBean implements Serializable {

    @Inject
    private transient AuthApiClient authApiClient;

    private String email;
    private String otp;
    private String motDePasse;
    private String confirmationMotDePasse;
    private boolean reinitialise;

    public String reinitialiser() {
        if (motDePasse != null && !motDePasse.equals(confirmationMotDePasse)) {
            FacesContext.getCurrentInstance()
                    .addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Les mots de passe ne correspondent pas.", null));
            return null;
        }
        try {
            authApiClient.reinitialiserMotDePasse(email, otp, motDePasse, confirmationMotDePasse);
            reinitialise = true;
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Réinitialisation impossible", e.getMessage()));
        }
        return null;
    }

    public boolean isReinitialise() {
        return reinitialise;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getConfirmationMotDePasse() {
        return confirmationMotDePasse;
    }

    public void setConfirmationMotDePasse(String confirmationMotDePasse) {
        this.confirmationMotDePasse = confirmationMotDePasse;
    }
}
