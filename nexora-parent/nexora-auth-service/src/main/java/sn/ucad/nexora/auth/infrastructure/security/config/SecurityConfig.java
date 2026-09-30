package sn.ucad.nexora.auth.infrastructure.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.ucad.nexora.auth.infrastructure.security.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/v1/auth/register").permitAll()
                .requestMatchers("/api/v1/auth/verify-otp").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/forgot-password").permitAll()
                .requestMatchers("/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/auth/logout").permitAll()
                
                .requestMatchers("/actuator/**")
                .permitAll()

                .requestMatchers("/api/v1/users/internal")
                .permitAll()

                // Administration (docs/architecture-acteurs.md §6, §9.2) : une permission par action.
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/comptes", "/api/v1/admin/comptes/*")
                .hasAuthority("PERM_CONSULTER_UTILISATEURS")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/comptes/*/suspendre")
                .hasAuthority("PERM_SUSPENDRE_UTILISATEURS")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/comptes/*/reactiver")
                .hasAuthority("PERM_REACTIVER_UTILISATEURS")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/comptes/*/roles/**")
                .hasAuthority("PERM_GERER_ROLES")
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/roles")
                .hasAuthority("PERM_ACCEDER_BACK_OFFICE")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/roles/*/permissions/**")
                .hasAuthority("PERM_GERER_PERMISSIONS")
                .requestMatchers("/api/v1/admin/**")
                .denyAll()

                .anyRequest()
                .authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}