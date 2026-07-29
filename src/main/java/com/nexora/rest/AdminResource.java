package com.nexora.rest;

import com.nexora.common.security.Secured;
import com.nexora.dto.admin.*;
import com.nexora.service.AdminService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Ressource REST d'administration et de moderation. Protegee par JWT
 * ({@link Secured}) : reservee aux profils administrateur / moderateur.
 */
@Path("/admin")
@Secured
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    private AdminService adminService;

    /** GET /api/admin/stats */
    @GET
    @Path("/stats")
    public AdminStatsDTO stats() {
        return adminService.statistiquesGlobales();
    }

    /** GET /api/admin/repartition */
    @GET
    @Path("/repartition")
    public List<SegmentDTO> repartition() {
        return adminService.repartitionAnnonces();
    }

    /** GET /api/admin/espaces/pending */
    @GET
    @Path("/espaces/pending")
    public List<EspaceAdminDTO> enAttente() {
        return adminService.espacesEnAttente();
    }

    /** POST /api/admin/espaces/{id}/valider */
    @POST
    @Path("/espaces/{id}/valider")
    public Response valider(@PathParam("id") Long id) {
        adminService.validerEspace(id);
        return Response.ok().build();
    }

    /** POST /api/admin/espaces/{id}/certifier?valeur=true */
    @POST
    @Path("/espaces/{id}/certifier")
    public Response certifier(@PathParam("id") Long id,
                             @QueryParam("valeur") @DefaultValue("true") boolean valeur) {
        adminService.certifierEspace(id, valeur);
        return Response.ok().build();
    }

    /** GET /api/admin/litiges */
    @GET
    @Path("/litiges")
    public List<LitigeDTO> litiges() {
        return adminService.litigesOuverts();
    }

    /** POST /api/admin/litiges/{id}/arbitrer */
    @POST
    @Path("/litiges/{id}/arbitrer")
    public Response arbitrer(@PathParam("id") Long id, ArbitrageRequest req) {
        adminService.arbitrerLitige(id, req.getStatut(), req.getDecision());
        return Response.ok().build();
    }
}
