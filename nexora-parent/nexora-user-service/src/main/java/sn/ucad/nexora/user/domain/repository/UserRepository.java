package sn.ucad.nexora.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import sn.ucad.nexora.user.domain.entity.User;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByAccountId(UUID accountId);

    User save(User user);

    void deleteById(UUID id);
}