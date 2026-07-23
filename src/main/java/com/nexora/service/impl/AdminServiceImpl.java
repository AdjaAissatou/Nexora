package com.nexora.service.impl;

import com.nexora.common.exception.ResourceNotFoundException;
import com.nexora.domain.dispute.Litige;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.dto.admin.*;
import com.nexora.repository.EspaceProfessionnelDao;
import com.nexora.repository.LitigeDao;
import com.nexora.repository.StatDao;
import com.nexora.service.AdminService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation transactionnelle des cas d'usage d'administration et de
 * moderation : statistiques, validation des espaces, arbitrage des litiges.
 */
@Stateless
public class AdminServiceImpl implements AdminService {

    @Inject
    private StatDao statDao;
    @Inject
    private EspaceProfessionnelDao espaceDao;
    @Inject
    private LitigeDao litigeDao;

    @Override
    public AdminStatsDTO statistiquesGlobales() {
        return new AdminStatsDTO(
                statDao.countUtilisateurs(),
                statDao.countEspaces(),
                statDao.countProduits(),
                statDao.countServices(),
                statDao.countEspaces(),
                statDao.countLitigesOuverts());
    }

    @Override
    public List<SegmentDTO> repartitionAnnonces() {
        return List.of(
                new SegmentDTO("Produits", statDao.countProduits()),
                new SegmentDTO("Services", statDao.countServices()),
                new SegmentDTO("Espaces", statDao.countEspaces()),
                new SegmentDTO("Promotions", statDao.countPromotions()));
    }

    @Override
    public List<EspaceAdminDTO> espacesEnAttente() {
        return espaceDao.findNonVerifies().stream()
                .map(e -> new EspaceAdminDTO(e, espaceDao.countOffres(e.getIdEspace())))
                .toList();
    }

    @Override
    public void validerEspace(Long idEspace) {
        EspaceProfessionnel e = espaceDao.findById(idEspace)
                .orElseThrow(() -> new ResourceNotFoundException("EspaceProfessionnel", idEspace));
        e.setVerifie(true);
        espaceDao.update(e);
    }

    @Override
    public void certifierEspace(Long idEspace, boolean certifie) {
        EspaceProfessionnel e = espaceDao.findById(idEspace)
                .orElseThrow(() -> new ResourceNotFoundException("EspaceProfessionnel", idEspace));
        e.setCertifie(certifie);
        espaceDao.update(e);
    }

    @Override
    public List<LitigeDTO> litigesOuverts() {
        return litigeDao.findOuverts().stream().map(LitigeDTO::new).toList();
    }

    @Override
    public void arbitrerLitige(Long idLitige, String statut, String decision) {
        Litige l = litigeDao.findById(idLitige)
                .orElseThrow(() -> new ResourceNotFoundException("Litige", idLitige));
        l.setStatut(statut != null ? statut : "RESOLU");
        l.setDecisionAdmin(decision);
        litigeDao.update(l);
    }
}
