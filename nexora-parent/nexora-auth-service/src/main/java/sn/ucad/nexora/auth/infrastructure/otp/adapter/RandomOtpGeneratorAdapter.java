package sn.ucad.nexora.auth.infrastructure.otp.adapter;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.application.port.outbound.OtpGeneratorPort;

@Component
public class RandomOtpGeneratorAdapter implements OtpGeneratorPort {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generateOtp() {

        int otp = 100000 + RANDOM.nextInt(900000);

        return String.valueOf(otp);
    }

}