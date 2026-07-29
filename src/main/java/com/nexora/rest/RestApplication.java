package com.nexora.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** Active JAX-RS. Toutes les ressources REST sont servies sous {@code /api}. */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
