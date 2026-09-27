package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data pour la résolution de l'identifiant utilisateur.
 * Résout le UUID du JWT (accountId) → id_utilisateur (Long).
 */
public interface UtilisateurLookupRepository extends JpaRepository<UtilisateurLookupEntity, Long> {

    @Query("SELECT u.id FROM UtilisateurLookupEntity u WHERE u.accountId = :accountId")
    Optional<Long> findIdByAccountId(@Param("accountId") UUID accountId);
}
