package com.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Set;

/**
 * Bean de la page de connexion / inscription (inscription a la demande).
 *
 * <p>Demo : toute paire email + mot de passe ouvre une session. En production
 * ce bean deleguerait a {@code AuthService} (BCrypt + JWT deja en place).</p>
 */
@Named("auth")
@ViewScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Vues de destination autorisees apres connexion. */
    private static final Set<String> DEST_OK = Set.of("creer-espace", "mon-espace", "accueil", "explorer");

    @Inject
    private SessionBean session;

    private String email;
    private String motDePasse;
    private String nom;
    private String mode = "login";        // login | register
    private String dest;                  // vue a atteindre apres connexion

    public String seConnecter() {
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            message("Email et mot de passe requis.");
            return null;
        }
        String affiche = (nom != null && !nom.isBlank()) ? nom : email.split("@")[0];
        session.connecter(affiche);
        String cible = (dest != null && DEST_OK.contains(dest)) ? dest : "accueil";
        return cible + "?faces-redirect=true";
    }

    public void basculer() {
        this.mode = "login".equals(mode) ? "register" : "login";
    }

    public boolean isRegister() { return "register".equals(mode); }

    private void message(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    // --- Getters / Setters ---
    public String getEmail()            { return email; }
    public void setEmail(String v)      { this.email = v; }
    public String getMotDePasse()       { return motDePasse; }
    public void setMotDePasse(String v) { this.motDePasse = v; }
    public String getNom()              { return nom; }
    public void setNom(String v)        { this.nom = v; }
    public String getMode()             { return mode; }
    public void setMode(String v)       { this.mode = v; }
    /** Lit la destination depuis l'URL (?dest=...) au premier rendu, puis la conserve. */
    public String getDest() {
        if (dest == null) {
            Object p = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestParameterMap().get("dest");
            if (p != null) dest = p.toString();
        }
        return dest;
    }
    public void setDest(String v)       { this.dest = v; }
}
