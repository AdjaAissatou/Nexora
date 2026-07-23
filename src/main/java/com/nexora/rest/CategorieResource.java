package com.nexora.rest;

import com.nexora.dto.CategorieDTO;
import com.nexora.service.CategorieService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/** Ressource REST publique des categories (accueil et filtres du client). */
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategorieResource {

    @Inject
    private CategorieService categorieService;

    /** GET /api/categories */
    @GET
    public List<CategorieDTO> lister() {
        return categorieService.categoriesRacines();
    }
}
