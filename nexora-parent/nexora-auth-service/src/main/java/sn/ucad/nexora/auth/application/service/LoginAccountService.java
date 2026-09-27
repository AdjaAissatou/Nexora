package sn.ucad.nexora.auth.application.service;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.auth.application.dto.request.LoginRequest;
import sn.ucad.nexora.auth.application.dto.response.AccountResponse;
import sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse;
import sn.ucad.nexora.auth.application.port.outbound.JwtProviderPort;
import sn.ucad.nexora.auth.application.port.outbound.PasswordEncoderPort;
import sn.ucad.nexora.auth.application.usecase.LoginUseCase;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.common.exception.BusinessException;

@Service
public class LoginAccountService implements LoginUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtProviderPort jwtProvider;

    public LoginAccountService(
            AccountRepository accountRepository,
            PasswordEncoderPort passwordEncoder,
            JwtProviderPort jwtProvider) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {

        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new BusinessException(
                    "L'identifiant et le mot de passe sont obligatoires."
            );
        }

        String username = request.getUsername().trim();

        /*
         * L'utilisateur peut se connecter avec :
         * - son adresse email
         * - son numéro de téléphone
         */

        Account account = accountRepository
                .findByEmail(username.toLowerCase())
                .orElseGet(() ->
                        accountRepository
                                .findByPhone(username)
                                .orElseThrow(() ->
                                        new BusinessException(
                                                "Email/téléphone ou mot de passe incorrect."
                                        )
                                )
                );

        /*
         * Vérification de l'état du compte
         */

        if (!account.isEnabled()) {
            throw new BusinessException(
                    "Votre compte est désactivé."
            );
        }

        if (account.isLocked()) {
            throw new BusinessException(
                    "Votre compte est bloqué."
            );
        }

        if (!account.isVerified()) {
            throw new BusinessException(
                    "Votre compte n'est pas encore vérifié."
            );
        }

        /*
         * Vérification du mot de passe
         */

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword())) {

            throw new BusinessException(
                    "Email/téléphone ou mot de passe incorrect."
            );
        }

        /*
         * Génération des tokens
         */

        String accessToken =
                jwtProvider.generateAccessToken(account);

        String refreshToken =
                jwtProvider.generateRefreshToken(account);

        /*
         * Construction de la réponse
         */

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setId(account.getId());
        accountResponse.setFirstName(account.getFirstName());
        accountResponse.setLastName(account.getLastName());
        accountResponse.setEmail(account.getEmail());
        accountResponse.setPhone(account.getPhone());

        AuthenticationResponse response =
                new AuthenticationResponse();

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setAccount(accountResponse);

        return response;
    }
}