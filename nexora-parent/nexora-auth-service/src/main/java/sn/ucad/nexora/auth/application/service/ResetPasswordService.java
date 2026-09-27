package sn.ucad.nexora.auth.application.service;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.auth.application.command.ResetPasswordCommand;
import sn.ucad.nexora.auth.application.port.outbound.PasswordEncoderPort;
import sn.ucad.nexora.auth.application.usecase.ResetPasswordUseCase;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.entity.OtpCode;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.auth.domain.repository.OtpCodeRepository;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

@Service
public class ResetPasswordService implements ResetPasswordUseCase {

    private final AccountRepository accountRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoderPort passwordEncoder;

    public ResetPasswordService(
            AccountRepository accountRepository,
            OtpCodeRepository otpCodeRepository,
            PasswordEncoderPort passwordEncoder) {

        this.accountRepository = accountRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {

        if (command == null) {
            throw new BusinessException("La demande de réinitialisation est obligatoire.");
        }

        if (command.getPassword() == null || !command.getPassword().equals(command.getConfirmPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas.");
        }

        String email = command.getEmail() == null ? "" : command.getEmail().trim().toLowerCase();
        String code = command.getOtp() == null ? "" : command.getOtp().trim();

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte associé à cette adresse email."));

        OtpCode otp = otpCodeRepository
                .findByAccountIdAndCode(account.getId(), code)
                .orElseThrow(() -> new BusinessException("Code OTP invalide."));

        if (otp.isExpired()) {
            throw new BusinessException("Le code OTP a expiré.");
        }
        if (otp.isUsed()) {
            throw new BusinessException("Le code OTP a déjà été utilisé.");
        }

        otp.use();
        otpCodeRepository.save(otp);

        account.changePassword(passwordEncoder.encode(command.getPassword()));
        accountRepository.save(account);
    }
}
