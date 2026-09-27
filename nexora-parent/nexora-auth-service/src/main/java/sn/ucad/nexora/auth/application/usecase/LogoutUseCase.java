package sn.ucad.nexora.auth.application.usecase;

public interface LogoutUseCase {

    void logout(String refreshToken);

}