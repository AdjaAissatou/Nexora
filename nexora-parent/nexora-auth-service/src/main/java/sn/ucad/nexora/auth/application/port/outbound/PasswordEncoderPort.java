package sn.ucad.nexora.auth.application.port.outbound;

public interface PasswordEncoderPort {

    String encode(String password);

    boolean matches(String rawPassword, String encodedPassword);

}