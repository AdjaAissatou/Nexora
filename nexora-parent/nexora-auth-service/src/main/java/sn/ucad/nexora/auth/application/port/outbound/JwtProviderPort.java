package sn.ucad.nexora.auth.application.port.outbound;

import io.jsonwebtoken.Claims;
import sn.ucad.nexora.auth.domain.entity.Account;

public interface JwtProviderPort {

    String generateAccessToken(Account account);

    String generateRefreshToken(Account account);

    /** Jetons d'une session (§25) : {@code sid} permet de la révoquer depuis « Sécurité du compte ». */
    String generateAccessToken(Account account, String sid);

    String generateRefreshToken(Account account, String sid);
    
    Claims parseToken(String token);

}