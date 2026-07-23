package com.nexora.service;

import com.nexora.dto.admin.*;

import java.util.List;

/** Cas d'usage d'administration, moderation et statistiques de la plateforme. */
public interface AdminService {

    /** Indicateurs globaux (utilisateurs, fournisseurs, annonces, litiges). */
    AdminStatsDTO statistiquesGlobales();

    /** Repartition des annonces par nature (produits / services / espaces / promotions). */
    List<SegmentDTO> repartitionAnnonces();

    /** Espaces professionnels en attente de validation. */
    List<EspaceAdminDTO> espacesEnAttente();

    /** Valide (verifie) un espace professionnel. */
    void validerEspace(Long idEspace);

    /** Active ou retire le badge "certifie" d'un espace. */
    void certifierEspace(Long idEspace, boolean certifie);

    /** Litiges ouverts a arbitrer. */
    List<LitigeDTO> litigesOuverts();

    /** Enregistre la decision d'arbitrage d'un litige. */
    void arbitrerLitige(Long idLitige, String statut, String decision);
}
