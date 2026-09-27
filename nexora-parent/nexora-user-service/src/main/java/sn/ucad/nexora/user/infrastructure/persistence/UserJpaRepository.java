package sn.ucad.nexora.user.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.ucad.nexora.user.infrastructure.persistence.entity.UserJpaEntity;

public interface UserJpaRepository
        extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByAccountId(UUID accountId);
}