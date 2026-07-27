package com.nexora.service.impl;

import com.nexora.common.enums.StatutOffre;
import com.nexora.common.exception.ResourceNotFoundException;
import com.nexora.domain.catalog.Offre;
import com.nexora.domain.dispute.Litige;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.dto.OffreDTO;
import com.nexora.dto.OffreMapper;
import com.nexora.dto.admin.*;
import com.nexora.repository.EspaceProfessionnelDao;
import com.nexora.repository.LitigeDao;
import com.nexora.repository.StatDao;
import com.nexora.service.AdminService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

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
    public List<OffreDTO> offresEnAttente() {
        return em.createQuery(
                        "select o from Offre o where o.statut = :st order by o.dateCreation desc",
                        Offre.class)
                .setParameter("st", StatutOffre.EN_ATTENTE_VALIDATION)
                .getResultList()
                .stream().map(OffreMapper::toDto).toList();
    }

    @Override
    public void validerOffre(Long idOffre) {
        Offre o = em.find(Offre.class, idOffre);
        if (o == null) throw new ResourceNotFoundException("Offre", idOffre);
        o.setStatut(StatutOffre.PUBLIEE);
        em.merge(o);
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
