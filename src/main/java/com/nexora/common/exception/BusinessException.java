package com.nexora.common.exception;

/** Exception metier (regle de gestion violee). Mappee en HTTP 400 cote REST. */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
