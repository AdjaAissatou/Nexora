package com.nexora.service;

import com.nexora.dto.ref.GeoDTO;
import com.nexora.dto.ref.RefItemDTO;

import java.util.List;

/**
 * Fournit les listes de reference alimentant les listes deroulantes des
 * formulaires (creation d'espace, publication d'offre) afin de minimiser la
 * saisie manuelle.
 */
public interface ReferenceService {

    List<RefItemDTO> typesEspace();

    List<RefItemDTO> categoriesEspace();

    List<RefItemDTO> typesOffre();

    List<RefItemDTO> modesPaiement();

    List<RefItemDTO> devises();

    List<RefItemDTO> pays();

    /** Regions d'un pays (ou toutes si {@code idPays} est null). */
    List<GeoDTO> regions(Long idPays);

    /** Villes d'une region (ou toutes si {@code idRegion} est null). */
    List<GeoDTO> villes(Long idRegion);
}
