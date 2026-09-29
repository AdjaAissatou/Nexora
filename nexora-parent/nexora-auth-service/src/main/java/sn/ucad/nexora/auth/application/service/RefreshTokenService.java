package sn.ucad.nexora.auth.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import sn.ucad.nexora.auth.application.dto.response.AccountResponse;
import sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse;
import sn.ucad.nexora.auth.application.port.outbound.JwtProviderPort;
import sn.ucad.nexora.auth.application.port.outbound.RevokedTokenRepository;
import sn.ucad.nexora.auth.application.usecase.RefreshTokenUseCase;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final AccountRepository accountRepository;
    private final JwtProviderPort jwtProvider;
    private final RevokedTokenRepository revokedTokenRepository;

    public RefreshTokenService(
            AccountRepository accountRepository,
            JwtProviderPort jwtProvider,
            RevokedTokenRepository revokedTokenRepository) {

        this.accountRepository = accountRepository;
        this.jwtProvider = jwtProvider;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {

        if (refreshToken == null ||
            refreshToken.isBlank()) {

            throw new IllegalArgumentException(
                    "Refresh token obligatoire"
            );
        }

        // 1. Vérification de la révocation
        if (revokedTokenRepository.isRevoked(refreshToken)) {

            throw new IllegalArgumentException(
                    "Refresh token révoqué"
            );
        }

        // 2. Vérification et lecture du JWT
        Claims claims =
                jwtProvider.parseToken(refreshToken);

        // 3. Vérification du type
        String tokenType =
                claims.get("type", String.class);

        if (!"REFRESH".equals(tokenType)) {

            throw new IllegalArgumentException(
                    "Le token fourni n'est pas un refresh token"
            );
        }

        // 4. Récupération du compte depuis le subject
        String subject =
                claims.getSubject();

        if (subject == null) {

            throw new IllegalArgumentException(
                    "Identifiant du compte absent du token"
            );
        }

        UUID accountId =
                UUID.fromString(subject);

        // 5. Récupération du compte
        Account account =
                accountRepository
                        .findById(accountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Compte introuvable"
                                )
                        );

        // 6. Vérification du compte
        if (!account.isEnabled()) {

            throw new IllegalArgumentException(
                    "Compte désactivé"
            );
        }

        if (account.isLocked()) {

            throw new IllegalArgumentException(
                    "Compte verrouillé"
            );
        }

        // 7. Génération d'un nouveau access token
        String newAccessToken =
                jwtProvider.generateAccessToken(account);

        // 8. Préparation de la réponse
        AuthenticationResponse response =
                new AuthenticationResponse();

        response.setAccessToken(newAccessToken);

        response.setRefreshToken(refreshToken);

        AccountResponse accountResponse =
                new AccountResponse();

        accountResponse.setId(account.getId());
        accountResponse.setFirstName(account.getFirstName());
        accountResponse.setLastName(account.getLastName());
        accountResponse.setEmail(account.getEmail());
        accountResponse.setPhone(account.getPhone());
        accountResponse.setRoles(account.getRoles().stream()
                .map(role -> role.getCode())
                .collect(java.util.stream.Collectors.toSet()));

        response.setAccount(accountResponse);

        return response;
    }
}