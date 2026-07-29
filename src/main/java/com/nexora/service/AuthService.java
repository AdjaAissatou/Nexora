package com.nexora.service;

import com.nexora.domain.user.Utilisateur;

/** Cas d'usage d'authentification et d'inscription. */
public interface AuthService {

    /** Inscrit un nouvel utilisateur (mot de passe hashe en BCrypt). */
    Utilisateur inscrire(String nom, String prenom, String email, String telephone, String motDePasse);

    /** Authentifie un utilisateur par email + mot de passe. */
    Utilisateur connecter(String email, String motDePasse);
}
