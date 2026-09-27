package sn.ucad.nexora.web.dto.auth;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.request.ResetPasswordRequest}. */
public record ResetPasswordRequest(String email, String otp, String password, String confirmPassword) {}
