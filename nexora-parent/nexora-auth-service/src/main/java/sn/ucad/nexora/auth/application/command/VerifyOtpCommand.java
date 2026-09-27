package sn.ucad.nexora.auth.application.command;

public class VerifyOtpCommand {

    private String email;

    private String otp;

    public VerifyOtpCommand() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

}