package sn.ucad.nexora.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import sn.ucad.nexora.auth.application.dto.request.LoginRequest;
import sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse;
import sn.ucad.nexora.auth.application.usecase.LoginUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginUseCase loginUseCase;

    public LoginController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthenticationResponse response =
                loginUseCase.login(request);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<String> me(
            @AuthenticationPrincipal UUID accountId) {

        return ResponseEntity.ok(
                "Utilisateur authentifié. Account ID : " + accountId
        );
    }
}