package sn.ucad.nexora.user.application.usecase;

import java.util.UUID;

import sn.ucad.nexora.user.application.dto.request.UpdateUserRequest;
import sn.ucad.nexora.user.domain.entity.User;

public interface UpdateUserUseCase {

    User update(UUID accountId, UpdateUserRequest request);
}
