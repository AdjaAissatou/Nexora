package sn.ucad.nexora.auth.application.usecase;

public interface ForgotPasswordUseCase {

    void sendResetCode(String email);

}