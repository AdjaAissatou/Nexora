package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.AuthApiClient;
import sn.ucad.nexora.web.dto.auth.RegisterRequest;
import sn.ucad.nexora.web.dto.auth.RegisterResult;
import sn.ucad.nexora.web.error.ApiException;

/** Backing bean de {@code inscription.xhtml}. */
@Named
@ViewScoped
public class InscriptionBean implements Serializable {

    @Inject
    private transient AuthApiClient authApiClient;

    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String motDePasse;
    private String confirmationMotDePasse;

    private boolean succes;
    private String messageSucces;
    private boolean verificationRequise;

    public String inscrire() {
        if (motDePasse != null && !motDePasse.equals(confirmationMotDePasse)) {
            FacesContext.getCurrentInstance()
                    .addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Les mots de passe ne correspondent pas.", null));
            return null;
        }
        try {
            RegisterResult resultat = authApiClient.inscrire(
                    new RegisterRequest(prenom, nom, email, telephone, motDePasse, confirmationMotDePasse, null));
            succes = true;
            verificationRequise = resultat.verificationRequired();
            messageSucces = resultat.message() != null && !resultat.message().isBlank()
                    ? resultat.message()
                    : "Compte créé. Vous pouvez maintenant vous connecter.";
            return null;
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Inscription impossible", e.getMessage()));
            return null;
        }
    }

    public boolean isSucces() {
        return succes;
    }

    public String getMessageSucces() {
        return messageSucces;
    }

    /** Vrai si le compte doit être vérifié par OTP avant de pouvoir se connecter. */
    public boolean isVerificationRequise() {
        return verificationRequise;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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
