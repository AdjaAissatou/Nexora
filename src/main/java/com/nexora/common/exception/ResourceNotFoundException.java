package com.nexora.common.exception;

/** Ressource introuvable. Mappee en HTTP 404 cote REST. */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String entite, Object id) {
        super(entite + " introuvable (id=" + id + ")");
    }
}
