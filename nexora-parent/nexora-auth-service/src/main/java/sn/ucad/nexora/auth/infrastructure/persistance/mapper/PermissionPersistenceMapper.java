package sn.ucad.nexora.auth.infrastructure.persistance.mapper;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.domain.entity.Permission;
import sn.ucad.nexora.auth.infrastructure.persistance.entity.PermissionEntity;

@Component
public class PermissionPersistenceMapper {

    public Permission toDomain(PermissionEntity entity) {

        if (entity == null)
            return null;

        Permission permission = new Permission();

        permission.setId(entity.getId());
        permission.setCode(entity.getCode());
        permission.setName(entity.getName());
        permission.setDescription(entity.getDescription());

        if (entity.isActive())
            permission.activate();
        else
            permission.deactivate();

        return permission;
    }

    public PermissionEntity toEntity(Permission domain) {

        if (domain == null)
            return null;

        PermissionEntity entity = new PermissionEntity();

        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());

        return entity;
    }

}