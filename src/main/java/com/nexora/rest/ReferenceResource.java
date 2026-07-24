package com.nexora.rest;

import com.nexora.dto.ref.GeoDTO;
import com.nexora.dto.ref.RefItemDTO;
import com.nexora.service.ReferenceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Ressource REST publique du referentiel : sert toutes les listes de reference
 * (types d'espace, categories d'espace, modes de paiement, devises, pays,
 * regions, villes) pour alimenter les listes deroulantes cote client.
 */
@Path("/ref")
@Produces(MediaType.APPLICATION_JSON)
public class ReferenceResource {

    @Inject
    private ReferenceService ref;

    @GET
    @Path("/types-espace")
    public List<RefItemDTO> typesEspace() {
        return ref.typesEspace();
    }

    @GET
    @Path("/categories-espace")
    public List<RefItemDTO> categoriesEspace() {
        return ref.categoriesEspace();
    }

    @GET
    @Path("/types-offre")
    public List<RefItemDTO> typesOffre() {
        return ref.typesOffre();
    }

    @GET
    @Path("/modes-paiement")
    public List<RefItemDTO> modesPaiement() {
        return ref.modesPaiement();
    }

    @GET
    @Path("/devises")
    public List<RefItemDTO> devises() {
        return ref.devises();
    }

    @GET
    @Path("/pays")
    public List<RefItemDTO> pays() {
        return ref.pays();
    }

    /** GET /api/ref/regions?pays={idPays} */
    @GET
    @Path("/regions")
    public List<GeoDTO> regions(@QueryParam("pays") Long idPays) {
        return ref.regions(idPays);
    }

    /** GET /api/ref/villes?region={idRegion} */
    @GET
    @Path("/villes")
    public List<GeoDTO> villes(@QueryParam("region") Long idRegion) {
        return ref.villes(idRegion);
    }
}
