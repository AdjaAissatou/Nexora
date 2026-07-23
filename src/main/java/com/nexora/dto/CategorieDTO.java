package com.nexora.dto;

import com.nexora.domain.catalog.CategorieOffre;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Vue exposee d'une categorie d'offre (menus, filtres, ecran d'accueil). */
@Getter
@Setter
public class CategorieDTO implements Serializable {

    private Long id;
    private String nom;
    private String slug;
    private String icone;
    private String couleur;
    private int niveau;
    private boolean populaire;

    public CategorieDTO() {
    }

    public CategorieDTO(CategorieOffre c) {
        this.id = c.getIdCategorie();
        this.nom = c.getNom();
        this.slug = c.getSlug();
        this.icone = c.getIcone();
        this.couleur = c.getCouleur();
        this.niveau = c.getNiveau();
        this.populaire = Boolean.TRUE.equals(c.getPopulaire());
    }
}
