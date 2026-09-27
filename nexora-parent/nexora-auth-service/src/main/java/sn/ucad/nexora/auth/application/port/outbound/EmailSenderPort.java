package sn.ucad.nexora.auth.application.port.outbound;

public interface EmailSenderPort {

    void sendOtp(String email, String otp);

}