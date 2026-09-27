package sn.ucad.nexora.auth.domain.entity;

import java.util.UUID;

import sn.ucad.nexora.common.domain.BaseDomainEntity;

public class Permission extends BaseDomainEntity{


    private String code;

    private String name;

    private String description;

    private boolean active = true;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
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

	public void setActive(boolean active) {
		this.active = active;
	}

}