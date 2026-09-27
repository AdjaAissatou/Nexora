package sn.ucad.nexora.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.ucad.nexora.auth.application.dto.request.ResetPasswordRequest;
import sn.ucad.nexora.auth.application.dto.response.OtpResponse;
import sn.ucad.nexora.auth.application.usecase.ResetPasswordUseCase;
import sn.ucad.nexora.auth.presentation.mapper.AuthPresentationMapper;

@RestController
@RequestMapping("/api/v1/auth")
public class ResetPasswordController {

    private final ResetPasswordUseCase resetPasswordUseCase;
    private final AuthPresentationMapper mapper;

    public ResetPasswordController(ResetPasswordUseCase resetPasswordUseCase, AuthPresentationMapper mapper) {
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<OtpResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        resetPasswordUseCase.resetPassword(mapper.toCommand(request));

        OtpResponse response = new OtpResponse();
        response.setMessage("Votre mot de passe a été réinitialisé avec succès.");

        return ResponseEntity.ok(response);
    }
}
