package com.nexora.rest;

import com.nexora.dto.CategorieDTO;
import com.nexora.dto.ref.AttributDTO;
import com.nexora.service.CategorieService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/** Ressource REST publique des categories (accueil, filtres, formulaires). */
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategorieResource {

    @Inject
    private CategorieService categorieService;

    /** GET /api/categories — categories racines (accueil). */
    @GET
    public List<CategorieDTO> lister() {
        return categorieService.categoriesRacines();
    }

    /** GET /api/categories/all — toutes les categories (liste deroulante). */
    @GET
    @Path("/all")
    public List<CategorieDTO> toutes() {
        return categorieService.toutesCategories();
    }

    /**
     * GET /api/categories/{id}/attributs — attributs dynamiques + valeurs
     * possibles, pour construire un formulaire majoritairement en dropdowns.
     */
    @GET
    @Path("/{id}/attributs")
    public List<AttributDTO> attributs(@PathParam("id") Long id) {
        return categorieService.attributsDeCategorie(id);
    }
}
