package sn.ucad.nexora.web.dto.auth;

import java.time.LocalDate;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.request.RegisterRequest}. */
public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String password,
        String confirmPassword,
        LocalDate birthDate) {}
