package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.ucad.nexora.auth.infrastructure.persistance.entity.RoleEntity;

public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByCode(String code);

}