package com.nexora.service;

import com.nexora.dto.OffreDTO;
import com.nexora.dto.PageResult;
import com.nexora.dto.RechercheCriteria;

/** Cas d'usage lies aux offres (recherche intelligente, consultation). */
public interface OffreService {

    /** Recherche multi-criteres paginee (mot-cle, prix, geo, EAV, tri). */
    PageResult<OffreDTO> rechercher(RechercheCriteria criteria);

    /** Consulte une offre et incremente son compteur de vues. */
    OffreDTO consulter(Long idOffre);
}
