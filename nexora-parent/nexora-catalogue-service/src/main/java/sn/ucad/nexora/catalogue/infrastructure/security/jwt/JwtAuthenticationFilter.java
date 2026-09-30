package sn.ucad.nexora.catalogue.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.ucad.nexora.common.security.AutoritesJwt;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProviderAdapter jwtProvider;

    public JwtAuthenticationFilter(JwtProviderAdapter jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtProvider.claims(token);
                // Un jeton de rafraîchissement (7 jours) ne doit ouvrir aucune route.
                if (AutoritesJwt.estJetonAcces(claims)) {
                    UUID accountId = UUID.fromString(claims.getSubject());
                    List<SimpleGrantedAuthority> autorites = new java.util.ArrayList<>();
                    autorites.add(new SimpleGrantedAuthority("ROLE_USER"));
                    // Rôles (ROLE_*) et permissions effectives (PERM_*) : docs/architecture-acteurs.md §6
                    AutoritesJwt.depuis(claims).forEach(a -> autorites.add(new SimpleGrantedAuthority(a)));
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(accountId, null, autorites));
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
