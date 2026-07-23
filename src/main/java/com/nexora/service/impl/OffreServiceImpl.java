package com.nexora.service.impl;

import com.nexora.common.exception.ResourceNotFoundException;
import com.nexora.domain.catalog.Offre;
import com.nexora.domain.space.Adresse;
import com.nexora.dto.*;
import com.nexora.repository.OffreDao;
import com.nexora.service.OffreService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Implementation transactionnelle (EJB Stateless) du service des offres.
 * Orchestre le DAO de recherche et applique la logique metier hors requete SQL
 * (raffinement de la distance par haversine, mapping DTO).
 */
@Stateless
public class OffreServiceImpl implements OffreService {

    @Inject
    private OffreDao offreDao;

    @Override
    public PageResult<OffreDTO> rechercher(RechercheCriteria criteria) {
        PageResult<Offre> page = offreDao.rechercher(criteria);

        List<OffreDTO> dtos = page.getContenu().stream().map(offre -> {
            OffreDTO dto = OffreMapper.toDto(offre);
            if (criteria.getLatitude() != null && criteria.getLongitude() != null) {
                dto.setDistanceKm(distanceLaPlusProche(offre, criteria));
            }
            return dto;
        }).toList();

        // Tri final par proximite (raffinement haversine hors SQL).
        if ("PROXIMITE".equals(criteria.getTri())) {
            dtos = dtos.stream()
                    .sorted((a, b) -> {
                        double da = a.getDistanceKm() == null ? Double.MAX_VALUE : a.getDistanceKm();
                        double db = b.getDistanceKm() == null ? Double.MAX_VALUE : b.getDistanceKm();
                        return Double.compare(da, db);
                    })
                    .toList();
        }
        return new PageResult<>(dtos, page.getTotal(), page.getPage(), page.getTaillePage());
    }

    @Override
    public OffreDTO consulter(Long idOffre) {
        Offre offre = offreDao.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException("Offre", idOffre));
        offre.setNbConsultation(offre.getNbConsultation() + 1);
        offreDao.update(offre);
        return OffreMapper.toDto(offre);
    }

    /** Distance haversine (km) vers l'adresse la plus proche de l'espace. */
    private Double distanceLaPlusProche(Offre offre, RechercheCriteria c) {
        if (offre.getEspace() == null) return null;
        return offre.getEspace().getAdresses().stream()
                .filter(a -> a.getLatitude() != null && a.getLongitude() != null)
                .mapToDouble(a -> haversine(c.getLatitude(), c.getLongitude(),
                        a.getLatitude(), a.getLongitude()))
                .min()
                .orElse(Double.NaN);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // rayon terrestre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
