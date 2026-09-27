package sn.ucad.nexora.auth.application.usecase.auth;

import sn.ucad.nexora.auth.application.command.VerifyOtpCommand;
import sn.ucad.nexora.auth.application.result.VerifyOtpResult;

public interface VerifyOtpUseCase {

    VerifyOtpResult verify(VerifyOtpCommand command);

}