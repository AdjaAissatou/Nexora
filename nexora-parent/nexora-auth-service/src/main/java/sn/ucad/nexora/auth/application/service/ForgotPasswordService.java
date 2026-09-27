package sn.ucad.nexora.auth.application.service;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.auth.application.port.outbound.ClockPort;
import sn.ucad.nexora.auth.application.port.outbound.EmailSenderPort;
import sn.ucad.nexora.auth.application.port.outbound.OtpGeneratorPort;
import sn.ucad.nexora.auth.application.usecase.ForgotPasswordUseCase;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.entity.OtpCode;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.auth.domain.repository.OtpCodeRepository;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

@Service
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final AccountRepository accountRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final OtpGeneratorPort otpGenerator;
    private final ClockPort clock;
    private final EmailSenderPort emailSender;

    public ForgotPasswordService(
            AccountRepository accountRepository,
            OtpCodeRepository otpCodeRepository,
            OtpGeneratorPort otpGenerator,
            ClockPort clock,
            EmailSenderPort emailSender) {

        this.accountRepository = accountRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
        this.emailSender = emailSender;
    }

    @Override
    public void sendResetCode(String email) {

        String cleanEmail = email == null ? "" : email.trim().toLowerCase();

        Account account = accountRepository
                .findByEmail(cleanEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucun compte associé à cette adresse email."
                        )
                );

        // Même mécanisme OTP que l'inscription (table otp_codes, expiration 10 minutes).
        OtpCode otp = new OtpCode();
        otp.setCode(otpGenerator.generateOtp());
        otp.setAccount(account);
        otp.setExpiresAt(clock.now().plusMinutes(10));

        otpCodeRepository.save(otp);

        emailSender.sendOtp(account.getEmail(), otp.getCode());
    }
}
