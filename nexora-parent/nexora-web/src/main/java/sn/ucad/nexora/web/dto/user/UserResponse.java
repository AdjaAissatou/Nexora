package sn.ucad.nexora.web.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

/** Miroir de {@code sn.ucad.nexora.user.application.dto.response.UserResponse}. */
public record UserResponse(
        Long id,
        UUID accountId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
