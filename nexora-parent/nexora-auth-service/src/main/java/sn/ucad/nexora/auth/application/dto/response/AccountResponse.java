package sn.ucad.nexora.auth.application.dto.response;

import java.util.Set;
import java.util.UUID;

public class AccountResponse {

    private UUID id;

    /** Codes des rôles du compte (UTILISATEUR, FOURNISSEUR, ADMIN...) — utilisés par le web pour adapter la navigation. */
    private Set<String> roles;

    /** Permissions effectives (union des permissions des rôles), pour adapter l'interface. */
    private Set<String> permissions;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

    // getters & setters

}