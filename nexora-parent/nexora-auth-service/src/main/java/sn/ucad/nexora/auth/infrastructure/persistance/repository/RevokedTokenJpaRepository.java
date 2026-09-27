package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.ucad.nexora.auth.infrastructure.persistance.entity.RevokedTokenEntity;

public interface RevokedTokenJpaRepository
        extends JpaRepository<RevokedTokenEntity, UUID> {

    boolean existsByTokenHash(String tokenHash);

    void deleteByExpiresAtBefore(Instant now);
}