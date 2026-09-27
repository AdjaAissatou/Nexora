package sn.ucad.nexora.auth.application.dto.response;

public class VerifyOtpResponse {

    private boolean verified;

    private String message;

    public VerifyOtpResponse() {
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}