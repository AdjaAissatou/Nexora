package sn.ucad.nexora.auth.application.port.outbound;

import io.jsonwebtoken.Claims;
import sn.ucad.nexora.auth.domain.entity.Account;

public interface JwtProviderPort {

    String generateAccessToken(Account account);

    String generateRefreshToken(Account account);
    
    Claims parseToken(String token);

}