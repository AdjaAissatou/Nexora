package com.nexora.web.bean;

import com.nexora.domain.user.Utilisateur;
import com.nexora.dto.EspaceViewDTO;
import com.nexora.service.AuthService;
import com.nexora.service.EspaceService;

import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Set;

/**
 * Bean de connexion / inscription. Aucune simulation : delegue a
 * {@link AuthService} (hachage BCrypt cote service).
 */
@Named("auth")
@ViewScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Set<String> DEST_OK = Set.of("creer-espace", "mon-espace", "accueil", "explorer");

    @Inject private AuthService authService;
    @Inject private EspaceService espaceService;
    @Inject private SessionBean session;

    private String email;
    private String motDePasse;
    private String nom;
    private String mode = "login";        // login | register
    private String dest;

    public String seConnecter() {
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            message("Email et mot de passe requis.");
            return null;
        }
        try {
            Utilisateur u = isRegister()
                    ? authService.inscrire(nom, null, email.trim(), null, motDePasse)
                    : authService.connecter(email.trim(), motDePasse);
            session.connecter(u);
            // Recharge l'espace existant de l'utilisateur (fournisseur qui revient).
            List<EspaceViewDTO> espaces = espaceService.mesEspaces(u.getIdUtilisateur());
            if (!espaces.isEmpty()) {
                EspaceViewDTO e = espaces.get(0);
                String ville = espaceService.parametres(e.getId()).getVille();
                session.setEspace(e, ville);
            }
        } catch (Exception ex) {
            message(isRegister()
                    ? "Inscription impossible : " + racine(ex)
                    : "Identifiants invalides.");
            return null;
        }
        String cible = (dest != null && DEST_OK.contains(dest)) ? dest : "accueil";
        return cible + "?faces-redirect=true";
    }

    public void basculer() {
        this.mode = "login".equals(mode) ? "register" : "login";
    }

    public boolean isRegister() { return "register".equals(mode); }

    private static String racine(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

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
