package sn.ucad.nexora.catalogue.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.ucad.nexora.catalogue.infrastructure.security.jwt.JwtAuthenticationFilter;

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
                        // Recherche et consultation disponibles sans authentification (GET uniquement :
                        // ces mêmes chemins portent aussi PUT/DELETE, qui doivent rester authentifiés).
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/offres/recherche",
                                "/api/v1/offres/{id:\\d+}",
                                "/api/v1/categories/**",
                                "/api/v1/lieux-publics/**",
                                "/api/v1/explorer",
                                "/api/v1/offres/recherche/facettes",
                                "/api/v1/offres/{id:\\d+}/similaires",
                                "/api/v1/offres/suggestions"
                        ).permitAll()
                        // Nexora Découvrir (§18) : flux et signaux anonymes, comme la recherche
                        .requestMatchers("/api/v1/decouvrir", "/api/v1/decouvrir/**").permitAll()
                        // Recherche par photo (§11) : publique comme la recherche par mots.
                        .requestMatchers(HttpMethod.POST, "/api/v1/offres/recherche-photo").permitAll()
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Gestion du catalogue (§9.11) : une permission par nature d'élément, la plus précise d'abord
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/catalogue/**")
                            .hasAnyAuthority("PERM_GERER_CATEGORIES", "PERM_GERER_TYPES_OFFRES", "PERM_GERER_ATTRIBUTS")
                        .requestMatchers("/api/v1/admin/catalogue/categories/*/types", "/api/v1/admin/catalogue/types/**")
                            .hasAuthority("PERM_GERER_TYPES_OFFRES")
                        .requestMatchers("/api/v1/admin/catalogue/categories/*/attributs", "/api/v1/admin/catalogue/attributs/**",
                                         "/api/v1/admin/catalogue/valeurs/**")
                            .hasAuthority("PERM_GERER_ATTRIBUTS")
                        .requestMatchers("/api/v1/admin/catalogue/**").hasAuthority("PERM_GERER_CATEGORIES")
                        // Modération des offres (docs/architecture-acteurs.md §9.9)
                        .requestMatchers("/api/v1/admin/offres", "/api/v1/admin/offres/**").hasAuthority("PERM_MODERER_OFFRES")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
