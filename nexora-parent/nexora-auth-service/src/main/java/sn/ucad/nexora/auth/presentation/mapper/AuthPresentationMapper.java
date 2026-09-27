package sn.ucad.nexora.auth.presentation.mapper;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.application.command.LoginCommand;
import sn.ucad.nexora.auth.application.command.RegisterCommand;
import sn.ucad.nexora.auth.application.command.VerifyOtpCommand;
import sn.ucad.nexora.auth.application.dto.request.LoginRequest;
import sn.ucad.nexora.auth.application.dto.request.RegisterRequest;
import sn.ucad.nexora.auth.application.dto.request.VerifyOtpRequest;
import sn.ucad.nexora.auth.application.dto.response.VerifyOtpResponse;
import sn.ucad.nexora.auth.application.result.VerifyOtpResult;
@Component
public class AuthPresentationMapper {

    public RegisterCommand toCommand(RegisterRequest request) {

        if (request == null) {
            return null;
        }

        RegisterCommand command = new RegisterCommand();

        command.setFirstName(request.getFirstName());
        command.setLastName(request.getLastName());
        command.setEmail(request.getEmail());
        command.setPhone(request.getPhone());
        command.setPassword(request.getPassword());
        command.setConfirmPassword(request.getConfirmPassword());
        command.setBirthDate(request.getBirthDate());

        return command;
    }

    public LoginCommand toCommand(LoginRequest request) {

        if (request == null) {
            return null;
        }

        LoginCommand command = new LoginCommand();

        command.setUsername(request.getUsername());
        command.setPassword(request.getPassword());

        return command;
    }
    public VerifyOtpCommand toCommand(VerifyOtpRequest request) {

        if (request == null) {
            return null;
        }

        VerifyOtpCommand command = new VerifyOtpCommand();

        command.setEmail(request.getEmail());
        command.setOtp(request.getOtp());

        return command;
    }
    public VerifyOtpResponse toResponse(VerifyOtpResult result) {

        if (result == null) {
            return null;
        }

        VerifyOtpResponse response = new VerifyOtpResponse();

        response.setVerified(result.isVerified());
        response.setMessage(result.getMessage());

        return response;
    }

}