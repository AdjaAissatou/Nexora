package com.nexora.common.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;
import java.util.Optional;

/**
 * Filtre JAX-RS applique aux ressources annotees {@link Secured}. Valide le
 * jeton Bearer et injecte l'identite dans le {@link SecurityContext}.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String auth = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            abort(ctx);
            return;
        }
        Optional<String> subject = jwtService.validateAndGetSubject(auth.substring(7).trim());
        if (subject.isEmpty()) {
            abort(ctx);
            return;
        }
        final String userId = subject.get();
        final boolean secure = ctx.getSecurityContext().isSecure();
        ctx.setSecurityContext(new SecurityContext() {
            public Principal getUserPrincipal() { return () -> userId; }
            public boolean isUserInRole(String role) { return true; }
            public boolean isSecure() { return secure; }
            public String getAuthenticationScheme() { return "Bearer"; }
        });
    }

    private void abort(ContainerRequestContext ctx) {
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"erreur\":\"Authentification requise\",\"code\":401}")
                .type("application/json").build());
    }
}
