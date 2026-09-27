package sn.ucad.nexora.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.ucad.nexora.auth.application.dto.request.ForgotPasswordRequest;
import sn.ucad.nexora.auth.application.dto.response.OtpResponse;
import sn.ucad.nexora.auth.application.usecase.ForgotPasswordUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class ForgotPasswordController {

    private final ForgotPasswordUseCase forgotPasswordUseCase;

    public ForgotPasswordController(ForgotPasswordUseCase forgotPasswordUseCase) {
        this.forgotPasswordUseCase = forgotPasswordUseCase;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<OtpResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        forgotPasswordUseCase.sendResetCode(request.getEmail());

        OtpResponse response = new OtpResponse();
        response.setMessage("Un code de réinitialisation a été envoyé à votre adresse email.");

        return ResponseEntity.ok(response);
    }
}
