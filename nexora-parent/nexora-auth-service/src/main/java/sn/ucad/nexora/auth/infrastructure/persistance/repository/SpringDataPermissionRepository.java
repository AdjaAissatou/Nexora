package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.ucad.nexora.auth.infrastructure.persistance.entity.PermissionEntity;

public interface SpringDataPermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    Optional<PermissionEntity> findByCode(String code);

}