package com.nexora.service;

import com.nexora.dto.OffreDTO;
import com.nexora.dto.OffreRequest;
import com.nexora.dto.PageResult;
import com.nexora.dto.RechercheCriteria;

/** Cas d'usage lies aux offres (recherche intelligente, consultation). */
public interface OffreService {

    /** Recherche multi-criteres paginee (mot-cle, prix, geo, EAV, tri). */
    PageResult<OffreDTO> rechercher(RechercheCriteria criteria);

    /** Consulte une offre et incremente son compteur de vues. */
    OffreDTO consulter(Long idOffre);

    /** Publie une nouvelle offre (produit ou service) sous un espace. */
    OffreDTO publier(OffreRequest req);

    /**
     * Met a jour une offre (titre, prix, categorie, attributs) et la renvoie
     * en validation (statut EN_ATTENTE_VALIDATION, motif de rejet efface).
     */
    OffreDTO modifier(Long idOffre, OffreRequest req);

    /** Toutes les offres d'un espace (tous statuts), pour la gestion "Mon espace". */
    java.util.List<OffreDTO> offresDeEspace(Long idEspace);
}
