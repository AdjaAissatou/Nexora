package com.nexora.repository;

import com.nexora.domain.user.Utilisateur;
import jakarta.ejb.Stateless;

import java.util.Optional;

/** DAO de l'entite {@link Utilisateur}. */
@Stateless
public class UtilisateurDao extends GenericDao<Utilisateur, Long> {

    public Optional<Utilisateur> findByEmail(String email) {
        return em().createQuery(
                        "select u from Utilisateur u where lower(u.email) = lower(:email)",
                        Utilisateur.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    public boolean emailExiste(String email) {
        Long n = em().createQuery(
                        "select count(u) from Utilisateur u where lower(u.email) = lower(:email)",
                        Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return n > 0;
    }
}
