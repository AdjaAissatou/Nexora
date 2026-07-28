package com.nexora.service.impl;

import com.nexora.common.exception.BusinessException;
import com.nexora.domain.space.Adresse;
import com.nexora.domain.space.CategorieEspace;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.domain.space.TypeEspaceProfessionnel;
import com.nexora.domain.user.Utilisateur;
import com.nexora.dto.EspaceRequest;
import com.nexora.dto.EspaceViewDTO;
import com.nexora.repository.EspaceProfessionnelDao;
import com.nexora.service.EspaceService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Implementation transactionnelle des espaces professionnels. Tout utilisateur
 * connecte peut creer un ou plusieurs espaces (boutique, clinique, service...)
 * qui deviennent la vitrine de ce qu'il propose.
 */
@Stateless
public class EspaceServiceImpl implements EspaceService {

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    @Inject
    private EspaceProfessionnelDao espaceDao;

    @Override
    public EspaceViewDTO creer(Long idProprietaire, EspaceRequest req) {
        if (req.getNomCommercial() == null || req.getNomCommercial().isBlank()) {
            throw new BusinessException("Le nom commercial est obligatoire.");
        }
        EspaceProfessionnel e = new EspaceProfessionnel();
        e.setNomCommercial(req.getNomCommercial());
        e.setNature(req.getNature() != null ? req.getNature() : "MIXTE");
        e.setDescription(req.getDescription());
        e.setTelephonePrincipal(req.getTelephonePrincipal());
        e.setEmail1(req.getEmail1());
        e.setSiteWeb(req.getSiteWeb());
        e.setWhatsapp(req.getWhatsapp());
        e.setLogo(req.getLogo());
        e.setSlug(genererSlug(req.getNomCommercial()));
        e.setVerifie(false);        // en attente de validation par un controleur
        e.setEtat(true);
        e.setProprietaire(em.getReference(Utilisateur.class, idProprietaire));
        e.setCreatedBy(String.valueOf(idProprietaire));
        if (req.getIdTypeEspace() != null) {
            e.setTypeEspace(em.getReference(TypeEspaceProfessionnel.class, req.getIdTypeEspace()));
        }
        if (req.getIdCategorieEspace() != null) {
            e.setCategorieEspace(em.getReference(CategorieEspace.class, req.getIdCategorieEspace()));
        }
        // Adresse principale (geolocalisee) si fournie.
        if (req.getLatitude() != null || req.getVille() != null) {
            Adresse a = new Adresse();
            a.setPays(req.getPays());
            a.setRegion(req.getRegion());
            a.setVille(req.getVille());
            a.setQuartier(req.getQuartier());
            a.setLatitude(req.getLatitude());
            a.setLongitude(req.getLongitude());
            a.setEspace(e);
            e.getAdresses().add(a);
        }
        espaceDao.save(e);
        return new EspaceViewDTO(e, 0);
    }

    @Override
    public List<EspaceViewDTO> mesEspaces(Long idProprietaire) {
        List<EspaceProfessionnel> espaces = em.createQuery(
                        "select e from EspaceProfessionnel e where e.proprietaire.idUtilisateur = :id "
                                + "order by e.createdAt desc", EspaceProfessionnel.class)
                .setParameter("id", idProprietaire)
                .getResultList();
        return espaces.stream()
                .map(e -> new EspaceViewDTO(e, espaceDao.countOffres(e.getIdEspace())))
                .toList();
    }

    private String genererSlug(String nom) {
        String base = Normalizer.normalize(nom, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return base + "-" + Long.toString(System.nanoTime(), 36);
    }
}
