package sn.ucad.nexora.auth.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.auth.application.port.outbound.UserServicePort;

import sn.ucad.nexora.auth.application.command.VerifyOtpCommand;
import sn.ucad.nexora.auth.application.result.VerifyOtpResult;
import sn.ucad.nexora.auth.application.usecase.auth.VerifyOtpUseCase;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.entity.OtpCode;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.auth.domain.repository.OtpCodeRepository;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

@Service
public class VerifyOtpService implements VerifyOtpUseCase {

    private final AccountRepository accountRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final UserServicePort userServicePort;
    
    public VerifyOtpService(
            AccountRepository accountRepository,
            OtpCodeRepository otpCodeRepository,
            UserServicePort userServicePort) {

        this.accountRepository = accountRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.userServicePort = userServicePort;
    }

    @Override
    public VerifyOtpResult verify(VerifyOtpCommand command) {

        if (command == null) {
            throw new BusinessException(
                    "La commande de vérification OTP est obligatoire."
            );
        }

        if (command.getEmail() == null || command.getEmail().isBlank()) {
            throw new BusinessException(
                    "L'adresse email est obligatoire."
            );
        }

        if (command.getOtp() == null || command.getOtp().isBlank()) {
            throw new BusinessException(
                    "Le code OTP est obligatoire."
            );
        }

        String email = command.getEmail()
                .trim()
                .toLowerCase();

        String code = command.getOtp()
                .trim();

        /*
         * 1. Recherche du compte
         */
        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucun compte associé à cette adresse email."
                        )
                );

        /*
         * 2. Recherche de l'OTP associé au compte
         */
        OtpCode otp = otpCodeRepository
                .findByAccountIdAndCode(
                        account.getId(),
                        code
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Code OTP invalide."
                        )
                );

        /*
         * 3. Vérification de l'expiration
         */
        if (otp.isExpired()) {
            throw new BusinessException(
                    "Le code OTP a expiré."
            );
        }

        /*
         * 4. Vérification de l'utilisation
         */
        if (otp.isUsed()) {
            throw new BusinessException(
                    "Le code OTP a déjà été utilisé."
            );
        }

        /*
         * 5. Consommation de l'OTP
         */
        otp.use();

        /*
         * 6. Activation du compte
         */
        account.activate();

        /*
         * 7. Sauvegarde
         */
        otpCodeRepository.save(otp);
        accountRepository.save(account);
        /*
         * 8. Création automatique du profil utilisateur
         */
        userServicePort.createUser(account);

        /*
         * 9. Résultat
         */
        VerifyOtpResult result = new VerifyOtpResult();

        result.setVerified(true);
        result.setMessage(
                "Votre compte a été vérifié avec succès."
        );

        return result;
    }
}