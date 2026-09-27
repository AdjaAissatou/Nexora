package sn.ucad.nexora.auth.infrastructure.persistance.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.domain.entity.Role;
import sn.ucad.nexora.auth.infrastructure.persistance.entity.RoleEntity;

@Component
public class RolePersistenceMapper {

    private final PermissionPersistenceMapper permissionMapper;

    public RolePersistenceMapper(PermissionPersistenceMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public Role toDomain(RoleEntity entity) {

        if (entity == null)
            return null;

        Role role = new Role();

        role.setId(entity.getId());
        role.setCode(entity.getCode());
        role.setName(entity.getName());
        role.setDescription(entity.getDescription());

        if (entity.isActive())
            role.activate();
        else
            role.deactivate();

        role.setPermissions(
                entity.getPermissions()
                        .stream()
                        .map(permissionMapper::toDomain)
                        .collect(Collectors.toSet()));

        return role;
    }

    public RoleEntity toEntity(Role domain) {

        if (domain == null)
            return null;

        RoleEntity entity = new RoleEntity();

        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());

        entity.setPermissions(
                domain.getPermissions()
                        .stream()
                        .map(permissionMapper::toEntity)
                        .collect(Collectors.toSet()));

        return entity;
    }

}