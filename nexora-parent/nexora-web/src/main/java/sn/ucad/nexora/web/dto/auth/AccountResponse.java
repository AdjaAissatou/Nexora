package sn.ucad.nexora.web.dto.auth;

import java.util.Set;
import java.util.UUID;

/**
 * Miroir de {@code sn.ucad.nexora.auth.application.dto.response.AccountResponse}.
 * {@code permissions} : permissions effectives du compte (union de celles de ses rôles, §6).
 */
public record AccountResponse(UUID id, String firstName, String lastName, String email, String phone, Set<String> roles,
                              Set<String> permissions) {

    public String nomComplet() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
