package com.nexora.rest;

import com.nexora.common.exception.BusinessException;
import com.nexora.common.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/** Traduit les exceptions metier en reponses HTTP JSON coherentes. */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException ex) {
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        if (ex instanceof ResourceNotFoundException) {
            status = Response.Status.NOT_FOUND;
        } else if (ex instanceof BusinessException) {
            status = Response.Status.BAD_REQUEST;
        }
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("erreur", ex.getMessage(), "code", status.getStatusCode()))
                .build();
    }
}
