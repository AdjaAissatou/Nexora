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
import sn.ucad.nexora.auth.infrastructure.persistance.securite.ConnexionsRepository;
import sn.ucad.nexora.common.audit.ClientHttp;
import sn.ucad.nexora.common.exception.BusinessException;

@Service
public class LoginAccountService implements LoginUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtProviderPort jwtProvider;
    private final ConnexionsRepository connexions;

    public LoginAccountService(
            AccountRepository accountRepository,
            PasswordEncoderPort passwordEncoder,
            JwtProviderPort jwtProvider,
            ConnexionsRepository connexions) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.connexions = connexions;
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

        jakarta.servlet.http.HttpServletRequest requete = ClientHttp.requeteCourante();
        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword())) {

            // Gardée pour l'alerte « tentatives échouées » de la page Sécurité (§25)
            connexions.echec(account.getId(), ClientHttp.adresseIp(requete), ClientHttp.agent(requete));

            throw new BusinessException(
                    "Email/téléphone ou mot de passe incorrect."
            );
        }

        /*
         * Génération des tokens
         */

        // Une session par connexion (§25) : son identifiant « sid » permet de la révoquer
        String sid = connexions.ouvrirSession(account.getId(), ClientHttp.adresseIp(requete), ClientHttp.agent(requete));

        String accessToken =
                jwtProvider.generateAccessToken(account, sid);

        String refreshToken =
                jwtProvider.generateRefreshToken(account, sid);

        /*
         * Construction de la réponse
         */

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setId(account.getId());
        accountResponse.setFirstName(account.getFirstName());
        accountResponse.setLastName(account.getLastName());
        accountResponse.setEmail(account.getEmail());
        accountResponse.setPhone(account.getPhone());
        accountResponse.setRoles(account.getRoles().stream()
                .map(role -> role.getCode())
                .collect(java.util.stream.Collectors.toSet()));
        accountResponse.setPermissions(account.effectivePermissions());

        AuthenticationResponse response =
                new AuthenticationResponse();

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setAccount(accountResponse);

        return response;
    }
}