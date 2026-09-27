package sn.ucad.nexora.auth.application.usecase.auth;

import sn.ucad.nexora.auth.application.command.RegisterCommand;
import sn.ucad.nexora.auth.application.result.RegisterResult;

public interface RegisterAccountUseCase {

    RegisterResult register(RegisterCommand command);

}