package sn.ucad.nexora.administration.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.ucad.nexora.administration.infrastructure.security.jwt.JwtAuthenticationFilter;

/** Accès par rôle administratif, selon le tableau §9.2 de docs/architecture-acteurs.md. */
@Configuration
public class SecurityConfig {

    /** Tous les rôles du back-office (AGENT_VERIFICATION n'en fait pas partie). */
    public static final String[] ROLES_BACK_OFFICE = {"SUPER_ADMIN", "ADMIN", "MODERATEUR", "SUPPORT", "GESTIONNAIRE"};

    private final JwtAuthenticationFilter jwt;

    public SecurityConfig(JwtAuthenticationFilter jwt) {
        this.jwt = jwt;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/v1/admin/tableau-de-bord").hasAnyRole(ROLES_BACK_OFFICE)
                        .requestMatchers("/api/v1/admin/journal", "/api/v1/admin/journal/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .anyRequest().denyAll())
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
