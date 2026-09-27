package sn.ucad.nexora.web.dto.auth;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.request.VerifyOtpRequest}. */
public record VerifyOtpRequest(String email, String otp) {}
