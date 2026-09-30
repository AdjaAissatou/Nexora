package sn.ucad.nexora.espace.infrastructure.security;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.http.HttpMethod; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; import sn.ucad.nexora.espace.infrastructure.security.jwt.JwtAuthenticationFilter;
@Configuration public class SecurityConfig{private final JwtAuthenticationFilter jwt;public SecurityConfig(JwtAuthenticationFilter j){jwt=j;}@Bean SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/actuator/**","/api/v1/types-espaces","/api/v1/geo/**").permitAll().requestMatchers(HttpMethod.GET,"/api/v1/espaces/{id:\\d+}").permitAll().requestMatchers(HttpMethod.POST,"/api/v1/espaces/{id:\\d+}/vue").permitAll()
  // Vérification (docs/architecture-acteurs.md §8.7) : chaque route exige une PERMISSION (§6), portée par le jeton.
  // Le téléchargement d'un justificatif est ouvert à tout compte connecté : l'accès est vérifié document par document.
  .requestMatchers(HttpMethod.GET,"/api/v1/verifications/{id:\\d+}/documents/{doc:\\d+}/fichier").authenticated()
  .requestMatchers("/api/v1/verifications","/api/v1/verifications/**").hasAuthority("PERM_TRAITER_VERIFICATIONS")
  .requestMatchers("/api/v1/admin/verifications/agents","/api/v1/admin/verifications/agents/**").hasAuthority("PERM_GERER_AGENTS_VERIFICATION")
  .requestMatchers("/api/v1/admin/verifications","/api/v1/admin/verifications/**").hasAuthority("PERM_SUPERVISER_VERIFICATIONS")
  // Modération des espaces (§9.9)
  .requestMatchers("/api/v1/admin/espaces","/api/v1/admin/espaces/**").hasAuthority("PERM_MODERER_ESPACES")
  .anyRequest().authenticated()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();}}
