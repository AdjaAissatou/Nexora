package com.nexora.common.security;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une ressource ou methode REST comme protegee : un jeton JWT valide
 * (en-tete {@code Authorization: Bearer ...}) est exige. Traite par
 * {@link AuthenticationFilter}.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
}
