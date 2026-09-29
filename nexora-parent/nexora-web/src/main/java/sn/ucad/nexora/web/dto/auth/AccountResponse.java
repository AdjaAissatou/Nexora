package sn.ucad.nexora.web.dto.auth;

import java.util.Set;
import java.util.UUID;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.response.AccountResponse}. */
public record AccountResponse(UUID id, String firstName, String lastName, String email, String phone, Set<String> roles) {

    public String nomComplet() {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
