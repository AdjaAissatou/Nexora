package com.nexora.rest;

import com.nexora.common.security.Secured;
import com.nexora.dto.EspaceRequest;
import com.nexora.dto.EspaceViewDTO;
import com.nexora.service.EspaceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

/**
 * Ressource REST des espaces professionnels. Toute connexion menant a un espace
 * utilisateur, l'utilisateur authentifie peut ici creer son (ou ses) espace(s)
 * professionnel(s) et lister ceux qu'il possede.
 */
@Path("/espaces")
@Secured
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EspaceResource {

    @Inject
    private EspaceService espaceService;

    @Context
    private SecurityContext securityContext;

    /** POST /api/espaces — cree un espace pour l'utilisateur connecte. */
    @POST
    public Response creer(EspaceRequest req) {
        Long idUser = currentUserId();
        EspaceViewDTO dto = espaceService.creer(idUser, req);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    /** GET /api/espaces/mes — les espaces de l'utilisateur connecte. */
    @GET
    @Path("/mes")
    public List<EspaceViewDTO> mesEspaces() {
        return espaceService.mesEspaces(currentUserId());
    }

    private Long currentUserId() {
        return Long.valueOf(securityContext.getUserPrincipal().getName());
    }
}
