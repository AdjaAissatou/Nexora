package sn.ucad.nexora.auth.infrastructure.security.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import sn.ucad.nexora.auth.application.port.outbound.JwtProviderPort;
import sn.ucad.nexora.auth.domain.entity.Account;

@Component
public class JwtProviderAdapter implements JwtProviderPort {

    private final SecretKey secretKey;

    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;

    public JwtProviderAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes:15}") long accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays) {

        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    @Override
    public String generateAccessToken(Account account) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(account.getId().toString())
                .claim("email", account.getEmail())
                .claim(
                        "roles",
                        account.getRoles()
                                .stream()
                                .map(role -> role.getCode())
                                .collect(Collectors.toSet())
                )
                .claim("permissions", account.effectivePermissions())
                .claim("type", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        accessTokenExpirationMinutes,
                                        ChronoUnit.MINUTES
                                )
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(Account account) {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(account.getId().toString())
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        refreshTokenExpirationDays,
                                        ChronoUnit.DAYS
                                )
                        )
                )
                .signWith(secretKey)
                .compact();
    }
    @Override
    public Claims parseToken(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}