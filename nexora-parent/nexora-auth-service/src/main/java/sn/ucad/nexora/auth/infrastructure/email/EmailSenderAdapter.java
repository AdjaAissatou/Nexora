package sn.ucad.nexora.auth.infrastructure.email;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.application.port.outbound.EmailSenderPort;

@Component
public class EmailSenderAdapter implements EmailSenderPort {

    @Override
    public void sendOtp(String email, String otp) {
        System.out.println("Envoi OTP à " + email + " : " + otp);
    }
}