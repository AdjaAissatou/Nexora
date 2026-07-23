package com.nexora.service.impl;

import com.nexora.common.exception.BusinessException;
import com.nexora.domain.user.Utilisateur;
import com.nexora.repository.UtilisateurDao;
import com.nexora.service.AuthService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

/**
 * Implementation de l'authentification. Les mots de passe ne sont jamais
 * stockes en clair : hachage BCrypt (salt integre) a l'inscription, comparaison
 * a la connexion.
 */
@Stateless
public class AuthServiceImpl implements AuthService {

    @Inject
    private UtilisateurDao utilisateurDao;

    @Override
    public Utilisateur inscrire(String nom, String prenom, String email,
                                String telephone, String motDePasse) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("L'email est obligatoire.");
        }
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new BusinessException("Le mot de passe doit contenir au moins 8 caracteres.");
        }
        if (utilisateurDao.emailExiste(email)) {
            throw new BusinessException("Un compte existe deja avec cet email.");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setTelephone(telephone);
        u.setMotDePasse(BCrypt.hashpw(motDePasse, BCrypt.gensalt(12)));
        u.setStatutCompte(Boolean.TRUE);
        u.setCreatedBy(email);
        return utilisateurDao.save(u);
    }

    @Override
    public Utilisateur connecter(String email, String motDePasse) {
        Utilisateur u = utilisateurDao.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Identifiants invalides."));
        if (!Boolean.TRUE.equals(u.getStatutCompte())) {
            throw new BusinessException("Compte desactive.");
        }
        if (!BCrypt.checkpw(motDePasse, u.getMotDePasse())) {
            throw new BusinessException("Identifiants invalides.");
        }
        u.setDerniereConnexion(LocalDateTime.now());
        return utilisateurDao.update(u);
    }
}
