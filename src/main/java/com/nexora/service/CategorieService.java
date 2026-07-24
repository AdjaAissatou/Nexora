package com.nexora.service;

import com.nexora.dto.CategorieDTO;
import com.nexora.dto.ref.AttributDTO;

import java.util.List;

/** Cas d'usage de consultation des categories (menus, filtres, accueil). */
public interface CategorieService {

    /** Categories racines visibles pour l'affichage public. */
    List<CategorieDTO> categoriesRacines();

    /** Toutes les categories visibles (pour les listes deroulantes). */
    List<CategorieDTO> toutesCategories();

    /**
     * Attributs dynamiques d'une categorie, avec leurs valeurs possibles :
     * alimente un formulaire de publication majoritairement en listes deroulantes.
     */
    List<AttributDTO> attributsDeCategorie(Long idCategorie);
}
