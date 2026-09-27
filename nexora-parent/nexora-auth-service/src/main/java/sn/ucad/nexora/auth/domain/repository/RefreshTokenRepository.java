package sn.ucad.nexora.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import sn.ucad.nexora.auth.domain.entity.RefreshToken;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken token);

    void deleteAllByAccountId(UUID accountId);

}