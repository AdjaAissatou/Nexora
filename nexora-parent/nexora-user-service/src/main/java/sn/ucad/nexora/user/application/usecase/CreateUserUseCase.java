package sn.ucad.nexora.user.application.usecase;

import sn.ucad.nexora.user.application.dto.request.CreateUserRequest;
import sn.ucad.nexora.user.domain.entity.User;

public interface CreateUserUseCase {

    User create(CreateUserRequest request);
}