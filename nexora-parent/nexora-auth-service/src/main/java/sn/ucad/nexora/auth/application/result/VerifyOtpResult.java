package sn.ucad.nexora.auth.application.result;

public class VerifyOtpResult {

    private String message;

    private boolean verified;

    public VerifyOtpResult() {
    }

    public String getMessage() {
        return message;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

}