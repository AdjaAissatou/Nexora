package sn.ucad.nexora.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.ucad.nexora.auth.application.dto.request.VerifyOtpRequest;
import sn.ucad.nexora.auth.application.dto.response.VerifyOtpResponse;
import sn.ucad.nexora.auth.application.result.VerifyOtpResult;
import sn.ucad.nexora.auth.application.usecase.auth.VerifyOtpUseCase;
import sn.ucad.nexora.auth.presentation.mapper.AuthPresentationMapper;

@RestController
@RequestMapping("/api/v1/auth")
public class VerifyOtpController {

    private final VerifyOtpUseCase verifyOtpUseCase;
    private final AuthPresentationMapper mapper;

    public VerifyOtpController(
            VerifyOtpUseCase verifyOtpUseCase,
            AuthPresentationMapper mapper) {

        this.verifyOtpUseCase = verifyOtpUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        VerifyOtpResult result =
                verifyOtpUseCase.verify(
                        mapper.toCommand(request)
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mapper.toResponse(result));
    }
}