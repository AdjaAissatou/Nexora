package sn.ucad.nexora.auth.application.usecase;

import sn.ucad.nexora.auth.application.dto.request.ResetPasswordRequest;

public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordRequest request);

}