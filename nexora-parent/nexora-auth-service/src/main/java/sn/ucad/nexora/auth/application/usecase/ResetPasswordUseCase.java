package sn.ucad.nexora.auth.application.usecase;

import sn.ucad.nexora.auth.application.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {

    void resetPassword(ResetPasswordCommand command);

}