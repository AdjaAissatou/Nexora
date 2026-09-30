package sn.ucad.nexora.administration.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Jeton d'accès → compte authentifié (UUID) avec ses rôles en autorités {@code ROLE_*}. */
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
            List<SimpleGrantedAuthority> autorites = new ArrayList<>();
            if (claims.get("roles") instanceof Collection<?> roles) {
                for (Object role : roles) if (role != null) autorites.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(compte, null, autorites));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(req, res);
    }
}
