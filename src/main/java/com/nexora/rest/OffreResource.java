package com.nexora.rest;

import com.nexora.dto.OffreDTO;
import com.nexora.dto.PageResult;
import com.nexora.dto.RechercheCriteria;
import com.nexora.service.OffreService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

/**
 * Ressource REST des offres, consommee par le client mobile (Android/iOS) et
 * tout tiers. Expose la recherche multi-criteres et la consultation.
 */
@Path("/offres")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OffreResource {

    @Inject
    private OffreService offreService;

    /** GET /api/offres?motCle=...&idCategorie=...&prixMax=...&lat=...&lng=...&rayon=...&tri=... */
    @GET
    public PageResult<OffreDTO> rechercher(
            @QueryParam("motCle") String motCle,
            @QueryParam("idCategorie") Long idCategorie,
            @QueryParam("idTypeOffre") Long idTypeOffre,
            @QueryParam("prixMin") BigDecimal prixMin,
            @QueryParam("prixMax") BigDecimal prixMax,
            @QueryParam("noteMin") Integer noteMin,
            @QueryParam("ville") String ville,
            @QueryParam("lat") Double latitude,
            @QueryParam("lng") Double longitude,
            @QueryParam("rayon") Double rayonKm,
            @QueryParam("verifie") Boolean verifie,
            @QueryParam("tri") @DefaultValue("PERTINENCE") String tri,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("taille") @DefaultValue("20") int taille) {

        RechercheCriteria c = new RechercheCriteria();
        c.setMotCle(motCle);
        c.setIdCategorie(idCategorie);
        c.setIdTypeOffre(idTypeOffre);
        c.setPrixMin(prixMin);
        c.setPrixMax(prixMax);
        c.setNoteMin(noteMin);
        c.setVille(ville);
        c.setLatitude(latitude);
        c.setLongitude(longitude);
        c.setDistanceMaxKm(rayonKm);
        c.setProfessionnelVerifie(verifie);
        c.setTri(tri);
        c.setPage(page);
        c.setTaillePage(taille);
        return offreService.rechercher(c);
    }

    /** GET /api/offres/{id} : detail d'une offre (incremente les vues). */
    @GET
    @Path("/{id}")
    public Response consulter(@PathParam("id") Long id) {
        return Response.ok(offreService.consulter(id)).build();
    }
}
