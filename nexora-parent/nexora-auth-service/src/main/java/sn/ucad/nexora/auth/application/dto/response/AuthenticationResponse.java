package sn.ucad.nexora.auth.application.dto.response;

public class AuthenticationResponse {

    private String accessToken;

    private String refreshToken;

    private AccountResponse account;

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

	public AccountResponse getAccount() {
		return account;
	}

	public void setAccount(AccountResponse account) {
		this.account = account;
	}

    // getters & setters

}