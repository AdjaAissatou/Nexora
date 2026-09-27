package sn.ucad.nexora.web.dto.auth;

/** Miroir de {@code sn.ucad.nexora.auth.application.result.RegisterResult}. */
public record RegisterResult(String message, String email, boolean verificationRequired) {}
