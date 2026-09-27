package sn.ucad.nexora.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.ucad.nexora.auth.application.dto.request.RefreshTokenRequest;
import sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse;
import sn.ucad.nexora.auth.application.usecase.RefreshTokenUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class RefreshTokenController {

    private final RefreshTokenUseCase refreshTokenUseCase;

    public RefreshTokenController(
            RefreshTokenUseCase refreshTokenUseCase) {

        this.refreshTokenUseCase =
                refreshTokenUseCase;
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthenticationResponse response =
                refreshTokenUseCase.refresh(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(response);
    }
}