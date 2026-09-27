package sn.ucad.nexora.auth.domain.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import sn.ucad.nexora.common.domain.BaseDomainEntity;

public class Role extends BaseDomainEntity{


    private String code;

    private String name;

    private String description;

    private boolean active = true;

    private Set<Permission> permissions = new HashSet<>();

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void assignPermission(Permission permission) {
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(permission ->
                        permission.getCode().equalsIgnoreCase(permissionCode));
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

}