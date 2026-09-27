package sn.ucad.nexora.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import sn.ucad.nexora.auth.domain.entity.Permission;

public interface PermissionRepository {

    Permission save(Permission permission);

    Optional<Permission> findById(UUID id);

    Optional<Permission> findByCode(String code);

    List<Permission> findAll();

}