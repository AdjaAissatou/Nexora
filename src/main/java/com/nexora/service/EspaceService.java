package com.nexora.service;

import com.nexora.dto.EspaceRequest;
import com.nexora.dto.EspaceViewDTO;

import java.util.List;

/** Cas d'usage des espaces professionnels (creation, consultation). */
public interface EspaceService {

    /** Cree un espace professionnel rattache a l'utilisateur proprietaire. */
    EspaceViewDTO creer(Long idProprietaire, EspaceRequest req);

    /** Liste les espaces professionnels d'un utilisateur. */
    List<EspaceViewDTO> mesEspaces(Long idProprietaire);

    /** Charge les parametres editables d'un espace (pre-remplissage du formulaire). */
    EspaceRequest parametres(Long idEspace);

    /** Met a jour les infos/parametres d'un espace (et son adresse principale). */
    EspaceViewDTO modifier(Long idEspace, EspaceRequest req);
}
