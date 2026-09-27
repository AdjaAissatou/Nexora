package sn.ucad.nexora.auth.application.port.outbound;

import java.util.Date;

public interface RevokedTokenRepository {

    void revoke(String refreshToken, Date expiresAt);

    boolean isRevoked(String refreshToken);
}