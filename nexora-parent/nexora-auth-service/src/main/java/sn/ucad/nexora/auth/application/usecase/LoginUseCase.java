package sn.ucad.nexora.auth.application.usecase;

import sn.ucad.nexora.auth.application.dto.request.LoginRequest;
import sn.ucad.nexora.auth.application.dto.response.AuthenticationResponse;

public interface LoginUseCase {

    AuthenticationResponse login(LoginRequest request);

}