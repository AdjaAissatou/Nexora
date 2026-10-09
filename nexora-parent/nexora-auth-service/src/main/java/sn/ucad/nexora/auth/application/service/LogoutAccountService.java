package sn.ucad.nexora.auth.application.service;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import sn.ucad.nexora.auth.application.port.outbound.RevokedTokenRepository;
import sn.ucad.nexora.auth.application.usecase.LogoutUseCase;
import sn.ucad.nexora.auth.infrastructure.security.jwt.JwtProviderAdapter;

@Service
public class LogoutAccountService implements LogoutUseCase {

    private final JwtProviderAdapter jwtProvider;
    private final RevokedTokenRepository revokedTokenRepository;
    private final sn.ucad.nexora.auth.infrastructure.persistance.securite.ConnexionsRepository connexions;

    public LogoutAccountService(
            JwtProviderAdapter jwtProvider,
            RevokedTokenRepository revokedTokenRepository,
            sn.ucad.nexora.auth.infrastructure.persistance.securite.ConnexionsRepository connexions) {

        this.jwtProvider = jwtProvider;
        this.revokedTokenRepository = revokedTokenRepository;
        this.connexions = connexions;
    }

    @Override
    public void logout(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Le refresh token est obligatoire."
            );
        }

        Claims claims = jwtProvider.parseToken(refreshToken);

        String tokenType = claims.get("type", String.class);

        if (!"REFRESH".equals(tokenType)) {
            throw new IllegalArgumentException(
                    "Le token fourni n'est pas un refresh token."
            );
        }

        revokedTokenRepository.revoke(
                refreshToken,
                claims.getExpiration()
        );
        // La session apparaît terminée dans « Sécurité du compte » (§25)
        connexions.terminer(java.util.UUID.fromString(claims.getSubject()), claims.get("sid", String.class), "DECONNEXION");
    }
}