package sn.ucad.nexora.web.dto.auth;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse}. */
public record AuthenticationResponse(String accessToken, String refreshToken, AccountResponse account) {}
