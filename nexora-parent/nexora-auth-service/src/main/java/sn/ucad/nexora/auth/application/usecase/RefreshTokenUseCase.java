package sn.ucad.nexora.auth.application.usecase;

import sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse;

public interface RefreshTokenUseCase {

    AuthenticationResponse refresh(String refreshToken);

}