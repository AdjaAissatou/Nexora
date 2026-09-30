package sn.ucad.nexora.recherche.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.ucad.nexora.recherche.infrastructure.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        // Lecture des avis (offre/espace) sans authentification
                        .requestMatchers(
                                "/api/v1/avis/offre/**",
                                "/api/v1/avis/espace/**",
                                "/api/v1/signalements/motifs",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/avis/{id:\\d+}").permitAll()
                        // Modération (docs/architecture-acteurs.md §9.10)
                        .requestMatchers("/api/v1/admin/avis", "/api/v1/admin/avis/**").hasAuthority("PERM_MODERER_AVIS")
                        .requestMatchers("/api/v1/admin/signalements", "/api/v1/admin/signalements/**").hasAuthority("PERM_GERER_SIGNALEMENTS")
                        // Tout le reste nécessite un JWT valide
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
