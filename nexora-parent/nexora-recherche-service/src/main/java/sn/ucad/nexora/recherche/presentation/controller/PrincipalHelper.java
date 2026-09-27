package sn.ucad.nexora.recherche.presentation.controller;

import org.springframework.security.core.Authentication;
import sn.ucad.nexora.recherche.infrastructure.persistence.UtilisateurLookupRepository;

import java.util.UUID;

/**
 * Utilitaire partagé par tous les controllers.
 * Résout le UUID du JWT (accountId) vers l'id_utilisateur (Long) de la table utilisateurs.
 */
class PrincipalHelper {

    static Long resolveUtilisateurId(Authentication auth,
                                     UtilisateurLookupRepository lookupRepository) {
        UUID accountId = (UUID) auth.getPrincipal();
        return lookupRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));
    }
}
