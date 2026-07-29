package com.nexora.service.impl;

import com.nexora.common.enums.StatutOffre;
import com.nexora.common.exception.ResourceNotFoundException;
import com.nexora.domain.attribute.Attribut;
import com.nexora.domain.attribute.OffreAttribut;
import com.nexora.domain.catalog.*;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.domain.space.Horaire;
import com.nexora.dto.*;
import com.nexora.repository.OffreDao;
import com.nexora.service.OffreService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation transactionnelle (EJB Stateless) du service des offres.
 * Orchestre le DAO de recherche, applique la logique metier hors requete SQL
 * (distance haversine, temps estime, statut ouvert) et gere la publication.
 */
@Stateless
public class OffreServiceImpl implements OffreService {

    /** Vitesse pieton moyenne (km/h) pour l'estimation du temps de trajet. */
    private static final double VITESSE_KMH = 6.0;

    @Inject
    private OffreDao offreDao;

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    @Override
    public PageResult<OffreDTO> rechercher(RechercheCriteria criteria) {
        PageResult<Offre> page = offreDao.rechercher(criteria);
        boolean geo = criteria.getLatitude() != null && criteria.getLongitude() != null;

        List<OffreDTO> dtos = page.getContenu().stream().map(offre -> {
            OffreDTO dto = OffreMapper.toDto(offre);
            if (geo) {
                double d = distanceLaPlusProche(offre, criteria);
                if (!Double.isNaN(d)) {
                    dto.setDistanceKm(Math.round(d * 10.0) / 10.0);
                    dto.setDureeEstimeeMin((int) Math.max(1, Math.round(d / VITESSE_KMH * 60)));
                }
            }
            dto.setOuvert(estOuvert(offre.getEspace()));
            return dto;
        }).toList();

        if ("PROXIMITE".equals(criteria.getTri())) {
            dtos = dtos.stream().sorted((a, b) -> {
                double da = a.getDistanceKm() == null ? Double.MAX_VALUE : a.getDistanceKm();
                double db = b.getDistanceKm() == null ? Double.MAX_VALUE : b.getDistanceKm();
                return Double.compare(da, db);
            }).toList();
        }
        return new PageResult<>(dtos, page.getTotal(), page.getPage(), page.getTaillePage());
    }

    @Override
    public OffreDTO consulter(Long idOffre) {
        Offre offre = offreDao.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException("Offre", idOffre));
        offre.setNbConsultation(offre.getNbConsultation() + 1);
        offreDao.update(offre);
        OffreDTO dto = OffreMapper.toDto(offre);
        dto.setOuvert(estOuvert(offre.getEspace()));
        return dto;
    }

    @Override
    public OffreDTO publier(OffreRequest req) {
        Offre offre = "SERVICE".equalsIgnoreCase(req.getType())
                ? nouveauService(req) : nouveauProduit(req);

        offre.setTitre(req.getTitre());
        offre.setDescription(req.getDescription());
        offre.setPrix(req.getPrix());
        offre.setDisponible(req.isDisponible());
        offre.setNegociable(req.isNegociable());
        offre.setStatut(StatutOffre.EN_ATTENTE_VALIDATION);
        offre.setDateCreation(LocalDateTime.now());
        offre.setEspace(em.getReference(EspaceProfessionnel.class, req.getIdEspace()));
        if (req.getIdCategorie() != null) {
            offre.setCategorie(em.getReference(CategorieOffre.class, req.getIdCategorie()));
        }
        if (req.getIdTypeOffre() != null) {
            offre.setTypeOffre(em.getReference(TypeOffre.class, req.getIdTypeOffre()));
        }
        em.persist(offre);

        // Photos : la premiere URL non vide devient l'image principale.
        if (req.getImages() != null) {
            int ordre = 0;
            for (String url : req.getImages()) {
                if (url == null || url.isBlank()) continue;
                Image img = new Image();
                img.setOffre(offre);
                img.setUrl(url.trim());
                img.setPrincipale(ordre == 0);
                img.setOrdre(ordre++);
                em.persist(img);
            }
        }

        // Valeurs d'attributs dynamiques (EAV) fournies sous forme texte.
        if (req.getAttributsTexte() != null) {
            for (Map.Entry<Long, String> e : req.getAttributsTexte().entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank()) continue;
                OffreAttribut oa = new OffreAttribut();
                oa.setOffre(offre);
                oa.setAttribut(em.getReference(Attribut.class, e.getKey()));
                oa.setValeurTexte(e.getValue());
                em.persist(oa);
            }
        }
        return OffreMapper.toDto(offre);
    }

    @Override
    public OffreDTO modifier(Long idOffre, OffreRequest req) {
        Offre offre = offreDao.findById(idOffre)
                .orElseThrow(() -> new ResourceNotFoundException("Offre", idOffre));
        offre.setTitre(req.getTitre());
        offre.setDescription(req.getDescription());
        offre.setPrix(req.getPrix());
        if (req.getIdCategorie() != null) {
            offre.setCategorie(em.getReference(CategorieOffre.class, req.getIdCategorie()));
        }
        offre.setStatut(StatutOffre.EN_ATTENTE_VALIDATION);
        offre.setMotifRejet(null);
        offre.setDateModification(LocalDateTime.now());
        // Remplacement des valeurs d'attributs (EAV).
        offre.getAttributs().clear();
        if (req.getAttributsTexte() != null) {
            for (Map.Entry<Long, String> e : req.getAttributsTexte().entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank()) continue;
                OffreAttribut oa = new OffreAttribut();
                oa.setOffre(offre);
                oa.setAttribut(em.getReference(Attribut.class, e.getKey()));
                oa.setValeurTexte(e.getValue());
                offre.getAttributs().add(oa);
            }
        }
        offreDao.update(offre);
        return OffreMapper.toDto(offre);
    }

    @Override
    public java.util.List<OffreDTO> offresDeEspace(Long idEspace) {
        List<Offre> offres = em.createQuery(
                        "select o from Offre o where o.espace.idEspace = :id order by o.dateCreation desc",
                        Offre.class)
                .setParameter("id", idEspace)
                .getResultList();
        return offres.stream().map(o -> {
            OffreDTO dto = OffreMapper.toDto(o);
            dto.setOuvert(estOuvert(o.getEspace()));
            return dto;
        }).toList();
    }

    private Produit nouveauProduit(OffreRequest req) {
        Produit p = new Produit();
        p.setMarque(req.getMarque());
        p.setModele(req.getModele());
        p.setNeuf(req.getNeuf());
        p.setStock(req.getStock());
        return p;
    }

    private ServicePro nouveauService(OffreRequest req) {
        ServicePro s = new ServicePro();
        s.setTypeService(req.getTypeService());
        s.setTarif(req.getTarif() != null ? req.getTarif() : req.getPrix());
        s.setADomicile(req.getADomicile());
        return s;
    }

    /** true si l'espace est ouvert a l'instant present d'apres ses horaires. */
    private boolean estOuvert(EspaceProfessionnel e) {
        if (e == null) return false;
        List<Horaire> horaires = e.getHoraires();
        if (horaires == null || horaires.isEmpty()) {
            return Boolean.TRUE.equals(e.getEtat()); // horaires inconnus : etat de l'espace
        }
        DayOfWeek jour = LocalDateTime.now().getDayOfWeek();
        LocalTime maintenant = LocalTime.now();
        return horaires.stream().anyMatch(h -> jour.equals(h.getJourSemaine())
                && h.getHeureOuverture() != null && h.getHeureFermeture() != null
                && !maintenant.isBefore(h.getHeureOuverture())
                && !maintenant.isAfter(h.getHeureFermeture()));
    }

    private double distanceLaPlusProche(Offre offre, RechercheCriteria c) {
        if (offre.getEspace() == null) return Double.NaN;
        return offre.getEspace().getAdresses().stream()
                .filter(a -> a.getLatitude() != null && a.getLongitude() != null)
                .mapToDouble(a -> haversine(c.getLatitude(), c.getLongitude(),
                        a.getLatitude(), a.getLongitude()))
                .min()
                .orElse(Double.NaN);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
