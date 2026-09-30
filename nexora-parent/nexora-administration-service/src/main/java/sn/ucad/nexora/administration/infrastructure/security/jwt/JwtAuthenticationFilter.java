package sn.ucad.nexora.administration.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.ucad.nexora.common.security.AutoritesJwt;

/** Jeton d'accès → compte authentifié (UUID), rôles en {@code ROLE_*} et permissions en {@code PERM_*}. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProviderAdapter jwt;

    public JwtAuthenticationFilter(JwtProviderAdapter jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String entete = req.getHeader("Authorization");
        if (entete == null || !entete.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }
        try {
            Claims claims = jwt.parseToken(entete.substring(7));
            if (!"ACCESS".equals(claims.get("type", String.class))) throw new IllegalArgumentException();
            UUID compte = UUID.fromString(claims.getSubject());
            List<SimpleGrantedAuthority> autorites = AutoritesJwt.depuis(claims).stream()
                    .map(SimpleGrantedAuthority::new).toList();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(compte, null, autorites));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(req, res);
    }
}
