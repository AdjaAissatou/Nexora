package sn.ucad.nexora.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import sn.ucad.nexora.auth.domain.entity.Role;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(UUID id);

    Optional<Role> findByCode(String code);

    List<Role> findAll();

}