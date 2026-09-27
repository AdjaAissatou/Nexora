package sn.ucad.nexora.auth.application.result;

public class AuthenticationResult {

    private String accessToken;

    private String refreshToken;

    private AccountResult account;

    public AuthenticationResult() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public AccountResult getAccount() {
        return account;
    }

    public void setAccount(AccountResult account) {
        this.account = account;
    }
}