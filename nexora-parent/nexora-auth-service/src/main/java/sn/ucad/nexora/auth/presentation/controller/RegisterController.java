package sn.ucad.nexora.auth.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.ucad.nexora.auth.application.command.RegisterCommand;
import sn.ucad.nexora.auth.application.dto.request.RegisterRequest;
import sn.ucad.nexora.auth.application.result.RegisterResult;
import sn.ucad.nexora.auth.application.usecase.auth.RegisterAccountUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class RegisterController {

    private final RegisterAccountUseCase registerAccountUseCase;

    public RegisterController(
            RegisterAccountUseCase registerAccountUseCase) {

        this.registerAccountUseCase = registerAccountUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResult> register(
            @RequestBody RegisterRequest request) {

        RegisterCommand command = new RegisterCommand();

        command.setFirstName(request.getFirstName());
        command.setLastName(request.getLastName());
        command.setEmail(request.getEmail());
        command.setPhone(request.getPhone());
        command.setPassword(request.getPassword());
        command.setConfirmPassword(request.getConfirmPassword());
        command.setBirthDate(request.getBirthDate());

        RegisterResult result =
                registerAccountUseCase.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}