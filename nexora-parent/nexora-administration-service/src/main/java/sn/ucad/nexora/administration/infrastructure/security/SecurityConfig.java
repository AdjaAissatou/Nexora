package sn.ucad.nexora.administration.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.ucad.nexora.administration.infrastructure.security.jwt.JwtAuthenticationFilter;

/** Chaque route exige une permission (docs/architecture-acteurs.md §6, §9.2), portée par le jeton. */
@Configuration
public class SecurityConfig {

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
                        .requestMatchers("/actuator/**", "/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/admin/tableau-de-bord").hasAuthority("PERM_ACCEDER_BACK_OFFICE")
                        .requestMatchers("/api/v1/admin/journal", "/api/v1/admin/journal/**").hasAuthority("PERM_GERER_JOURNAL")
                        .requestMatchers("/api/v1/admin/parametres", "/api/v1/admin/parametres/**").hasAuthority("PERM_GERER_PARAMETRES")
                        .anyRequest().denyAll())
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
