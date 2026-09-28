package sn.ucad.nexora.user.application.usecase;

import java.util.UUID;

import sn.ucad.nexora.user.domain.entity.User;

public interface GetUserUseCase {

    User getById(Long id);

    User getByAccountId(UUID accountId);
}
