package sn.ucad.nexora.auth.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.auth.application.command.RegisterCommand;
import sn.ucad.nexora.auth.application.port.outbound.ClockPort;
import sn.ucad.nexora.auth.application.port.outbound.EmailSenderPort;
import sn.ucad.nexora.auth.application.port.outbound.EventPublisherPort;
import sn.ucad.nexora.auth.application.port.outbound.OtpGeneratorPort;
import sn.ucad.nexora.auth.application.port.outbound.PasswordEncoderPort;
import sn.ucad.nexora.auth.application.result.RegisterResult;
import sn.ucad.nexora.auth.application.usecase.auth.RegisterAccountUseCase;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.entity.OtpCode;
import sn.ucad.nexora.auth.domain.entity.Role;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.auth.domain.repository.OtpCodeRepository;
import sn.ucad.nexora.auth.domain.repository.RoleRepository;
import sn.ucad.nexora.common.exception.BusinessException;

@Service
public class RegisterAccountService implements RegisterAccountUseCase {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final OtpCodeRepository otpCodeRepository;

    private final PasswordEncoderPort passwordEncoder;
    private final OtpGeneratorPort otpGenerator;
    private final ClockPort clock;
    private final EmailSenderPort emailSender;
    private final EventPublisherPort eventPublisher;

    /** Paramètre INSCRIPTIONS_OUVERTES (§9.12) : fermer les inscriptions depuis le back-office. */
    @org.springframework.beans.factory.annotation.Autowired
    private sn.ucad.nexora.common.parametre.Parametres parametres;

    public RegisterAccountService(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            OtpCodeRepository otpCodeRepository,
            PasswordEncoderPort passwordEncoder,
            OtpGeneratorPort otpGenerator,
            ClockPort clock,
            EmailSenderPort emailSender,
            EventPublisherPort eventPublisher) {

        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
        this.emailSender = emailSender;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegisterResult register(RegisterCommand command) {

        if (parametres != null && !parametres.booleen("INSCRIPTIONS_OUVERTES", true)) {
            throw new BusinessException("Les inscriptions sont momentanément fermées. Réessayez plus tard.");
        }

        // Vérification email
        if (accountRepository.existsByEmail(command.getEmail())) {
            throw new BusinessException("Cette adresse email est déjà utilisée.");
        }

        // Vérification téléphone
        if (accountRepository.existsByPhone(command.getPhone())) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé.");
        }

        // Vérification mot de passe
        if (!command.getPassword().equals(command.getConfirmPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas.");
        }

        // Récupération du rôle par défaut
        Role role = roleRepository.findByCode("UTILISATEUR")
                .orElseThrow(() ->
                        new BusinessException(
                                "Le rôle UTILISATEUR est introuvable."
                        ));
        // Création du compte
        Account account = new Account();

        account.setFirstName(command.getFirstName());
        account.setLastName(command.getLastName());
        account.setEmail(command.getEmail().trim().toLowerCase());
        account.setPhone(command.getPhone());
        account.setBirthDate(command.getBirthDate());

        account.changePassword(
                passwordEncoder.encode(command.getPassword())
        );

        account.assignRole(role);

        // Sauvegarde du compte
        account = accountRepository.save(account);

        // Génération OTP
        OtpCode otp = new OtpCode();

        otp.setCode(otpGenerator.generateOtp());
        otp.setAccount(account);

        LocalDateTime expiration = clock.now().plusMinutes(10);

        otp.setExpiresAt(expiration);

        otpCodeRepository.save(otp);

        // Envoi email
        emailSender.sendOtp(
                account.getEmail(),
                otp.getCode()
        );

        // Publication évènement
        eventPublisher.publish(account);

        // Résultat
        RegisterResult result = new RegisterResult();

        result.setEmail(account.getEmail());
        result.setMessage(
                "Votre compte a été créé avec succès. Vérifiez votre adresse email."
        );
        result.setVerificationRequired(true);

        return result;
    }

}