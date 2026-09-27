package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.application.port.outbound.RevokedTokenRepository;
import sn.ucad.nexora.auth.infrastructure.persistance.entity.RevokedTokenEntity;

@Component
public class RevokedTokenRepositoryAdapter
        implements RevokedTokenRepository {

    private final RevokedTokenJpaRepository repository;

    public RevokedTokenRepositoryAdapter(
            RevokedTokenJpaRepository repository) {

        this.repository = repository;
    }

    @Override
    public void revoke(
            String refreshToken,
            Date expiresAt) {

        String hash = hash(refreshToken);

        if (repository.existsByTokenHash(hash)) {
            return;
        }

        RevokedTokenEntity entity =
                new RevokedTokenEntity(
                        hash,
                        expiresAt.toInstant()
                );

        repository.save(entity);
    }

    @Override
    public boolean isRevoked(String refreshToken) {

        return repository.existsByTokenHash(
                hash(refreshToken)
        );
    }

    private String hash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {
                hex.append(
                        String.format("%02x", b)
                );
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 indisponible",
                    e
            );
        }
    }
}