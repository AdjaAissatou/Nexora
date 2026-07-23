package com.nexora.service;

import com.nexora.dto.CategorieDTO;

import java.util.List;

/** Cas d'usage de consultation des categories (menus, filtres, accueil). */
public interface CategorieService {

    /** Categories racines visibles pour l'affichage public. */
    List<CategorieDTO> categoriesRacines();
}
