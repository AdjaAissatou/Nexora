package sn.ucad.nexora.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.ucad.nexora.auth.application.dto.request.RefreshTokenRequest;
import sn.ucad.nexora.auth.application.usecase.LogoutUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final LogoutUseCase logoutUseCase;

    public LogoutController(
            LogoutUseCase logoutUseCase) {

        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshTokenRequest request) {

        logoutUseCase.logout(
                request.getRefreshToken()
        );

        return ResponseEntity.noContent().build();
    }
}