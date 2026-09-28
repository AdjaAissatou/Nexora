package sn.ucad.nexora.user.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import sn.ucad.nexora.user.application.dto.request.CreateUserRequest;
import sn.ucad.nexora.user.application.dto.request.UpdateUserRequest;
import sn.ucad.nexora.user.application.usecase.CreateUserUseCase;
import sn.ucad.nexora.user.application.usecase.UpdateUserUseCase;
import sn.ucad.nexora.user.application.dto.response.UserResponse;
import sn.ucad.nexora.user.application.usecase.GetUserUseCase;
import sn.ucad.nexora.user.domain.entity.User;
import sn.ucad.nexora.user.presentation.mapper.UserPresentationMapper;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UserPresentationMapper mapper;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    public UserController(
            GetUserUseCase getUserUseCase,
            CreateUserUseCase createUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            UserPresentationMapper mapper) {

        this.getUserUseCase = getUserUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.mapper = mapper;
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody UpdateUserRequest request) {

        User user = updateUserUseCase.update(accountId, request);

        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable("accountId") UUID accountId) {

        User user = getUserUseCase.getByAccountId(accountId);

        return ResponseEntity.ok(
                mapper.toResponse(user)
        );
    }
    @PostMapping("/internal")
    public ResponseEntity<UserResponse> createUser(
            @RequestBody CreateUserRequest request) {

        User user =
                createUserUseCase.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(user));
    }
}