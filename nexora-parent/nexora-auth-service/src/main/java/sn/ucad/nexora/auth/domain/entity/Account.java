package sn.ucad.nexora.auth.domain.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import sn.ucad.nexora.common.domain.BaseDomainEntity;

public class Account extends BaseDomainEntity{


    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String password;

    private LocalDate birthDate;

    private boolean enabled;

    private boolean verified;

    private boolean locked;

    private Set<Role> roles = new HashSet<>();

    public Account() {
        this.enabled = true;
        this.verified = false;
        this.locked = false;
    }

    /*=========================
            BUSINESS METHODS
    =========================*/

    public void verify() {
        this.verified = true;
    }

    public void unverify() {
        this.verified = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void assignRole(Role role) {
        if (role != null) {
            roles.add(role);
        }
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(roleCode));
    }

    /**
     * Permissions effectives du compte : union des permissions actives de ses rôles actifs
     * (docs/architecture-acteurs.md §6). Portées par le jeton d'accès, vérifiées par les services.
     */
    public java.util.Set<String> effectivePermissions() {
        return roles.stream()
                .filter(Role::isActive)
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::isActive)
                .map(Permission::getCode)
                .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));
    }

    public boolean hasPermission(String permissionCode) {

        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission ->
                        permission.getCode().equalsIgnoreCase(permissionCode));
    }

    /*=========================
            GETTERS & SETTERS
    =========================*/

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

    public String getPassword() {
        return password;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isLocked() {
        return locked;
    }

    public Set<Role> getRoles() {
        return roles;
    }

	public void setPassword(String password) {
		this.password = password;
	}




	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	public void activate() {
	    this.enabled = true;
	    this.verified = true;
	}
	public void deactivate() {
	    this.enabled = false;
	    this.verified = false;
	}
}