package sn.ucad.nexora.catalogue.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.UtilisateurLookupEntity;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurLookupRepository extends JpaRepository<UtilisateurLookupEntity, Long> {
    @Query("select u.id from UtilisateurLookupEntity u where u.accountId = :accountId")
    Optional<Long> findIdByAccountId(@Param("accountId") UUID accountId);
}
