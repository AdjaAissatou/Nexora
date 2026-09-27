package sn.ucad.nexora.web.dto.auth;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.response.VerifyOtpResponse}. */
public record VerifyOtpResponse(boolean verified, String message) {}
