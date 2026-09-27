package sn.ucad.nexora.user.infrastructure.security.jwt;

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

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProviderAdapter jwtProvider;

    public JwtAuthenticationFilter(
            JwtProviderAdapter jwtProvider) {

        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
    	System.out.println("===== JWT FILTER EXECUTE =====");
        String authorization =
                request.getHeader("Authorization");

        // Aucun token
        if (authorization == null ||
            !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {

            // Vérification du JWT
            Claims claims =
                    jwtProvider.parseToken(token);

            // Vérification du type
            String tokenType =
                    claims.get("type", String.class);

            if (!"ACCESS".equals(tokenType)) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            // Récupération du compte
            String subject =
                    claims.getSubject();

            if (subject == null) {

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            UUID accountId =
                    UUID.fromString(subject);

            // Récupération des rôles
            List<SimpleGrantedAuthority> authorities =
                    new ArrayList<>();

            Object rolesClaim =
                    claims.get("roles");

            if (rolesClaim instanceof Collection<?> roles) {

                for (Object role : roles) {

                    if (role != null) {

                        authorities.add(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role.toString()
                                )
                        );
                    }
                }
            }

            // Authentification Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            accountId,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            System.out.println(
            	    "JWT USER SERVICE - accountId = "
            	    + accountId
            	);

            	System.out.println(
            	    "JWT USER SERVICE - authorities = "
            	    + authorities
            	);

            	System.out.println(
            	    "JWT USER SERVICE - authenticated = "
            	    + authentication.isAuthenticated()
            	);
        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            System.out.println(
                "JWT USER SERVICE - ERREUR : "
                + e.getClass().getSimpleName()
                + " - "
                + e.getMessage()
            );
        }

        filterChain.doFilter(request, response);
    }
}