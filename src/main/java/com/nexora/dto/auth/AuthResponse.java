package com.nexora.dto.auth;

import com.nexora.domain.user.Utilisateur;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Reponse d'authentification : jeton JWT + resume de l'utilisateur. */
@Getter
@Setter
public class AuthResponse implements Serializable {

    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserSummary utilisateur;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresIn, Utilisateur u) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.utilisateur = new UserSummary(u);
    }

    /** Vue publique minimale d'un utilisateur (jamais le mot de passe). */
    @Getter
    @Setter
    public static class UserSummary implements Serializable {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String profile;

        public UserSummary() {
        }

        public UserSummary(Utilisateur u) {
            this.id = u.getIdUtilisateur();
            this.nom = u.getNom();
            this.prenom = u.getPrenom();
            this.email = u.getEmail();
            this.profile = u.getProfile() != null ? u.getProfile().getLibelle() : null;
        }
    }
}
