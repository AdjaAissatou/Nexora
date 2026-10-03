package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.dto.auth.VerifyOtpResponse;
import sn.ucad.nexora.web.error.ApiException;

/** Backing bean de {@code verification.xhtml} — saisie du code OTP reçu après inscription. */
@Named
@ViewScoped
public class VerificationBean implements Serializable {

    @Inject
    private transient AuthApiClient authApiClient;

    private String email;
    private String otp;
    private boolean verifie;

    public String verifier() {
        try {
            VerifyOtpResponse reponse = authApiClient.verifierOtp(email, otp);
            if (reponse.verified()) {
                verifie = true;
            } else {
                FacesContext.getCurrentInstance()
                        .addMessage(
                                null,
                                sn.ucad.nexora.web.util.Messages.complet(
                                        FacesMessage.SEVERITY_ERROR,
                                        "Code incorrect",
                                        reponse.message() != null ? reponse.message() : "Vérifiez le code reçu par email."));
            }
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Vérification impossible", e.getMessage()));
        }
        return null;
    }

    public boolean isVerifie() {
        return verifie;
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
}
