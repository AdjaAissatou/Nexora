package com.nexora.service.impl;

import com.nexora.domain.catalog.TypeOffre;
import com.nexora.domain.payment.ModePaiement;
import com.nexora.domain.reference.Devise;
import com.nexora.domain.reference.Pays;
import com.nexora.domain.reference.Region;
import com.nexora.domain.reference.Ville;
import com.nexora.domain.space.CategorieEspace;
import com.nexora.domain.space.TypeEspaceProfessionnel;
import com.nexora.dto.ref.GeoDTO;
import com.nexora.dto.ref.RefItemDTO;
import com.nexora.service.ReferenceService;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/** Implementation des listes de reference (lecture seule). */
@Stateless
public class ReferenceServiceImpl implements ReferenceService {

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    @Override
    public List<RefItemDTO> typesEspace() {
        return em.createQuery(
                        "select t from TypeEspaceProfessionnel t where t.actif = true order by t.libelle",
                        TypeEspaceProfessionnel.class)
                .getResultStream()
                .map(t -> new RefItemDTO((long) t.getIdTypeEspace(), t.getCode(), t.getLibelle()))
                .toList();
    }

    @Override
    public List<RefItemDTO> categoriesEspace() {
        return em.createQuery(
                        "select c from CategorieEspace c where c.actif = true order by c.ordreAffichage, c.nom",
                        CategorieEspace.class)
                .getResultStream()
                .map(c -> new RefItemDTO(c.getIdCategorieEspace(), c.getSlug(), c.getNom()))
                .toList();
    }

    @Override
    public List<RefItemDTO> typesOffre() {
        return em.createQuery("select t from TypeOffre t order by t.libelle", TypeOffre.class)
                .getResultStream()
                .map(t -> new RefItemDTO(t.getIdTypeOffre(), t.getLibelle(), t.getLibelle()))
                .toList();
    }

    @Override
    public List<RefItemDTO> modesPaiement() {
        return em.createQuery("select m from ModePaiement m order by m.libelle", ModePaiement.class)
                .getResultStream()
                .map(m -> new RefItemDTO((long) m.getIdModePaiement(), null, m.getLibelle()))
                .toList();
    }

    @Override
    public List<RefItemDTO> devises() {
        return em.createQuery("select d from Devise d order by d.code", Devise.class)
                .getResultStream()
                .map(d -> new RefItemDTO(d.getIdDevise(), d.getCode(), d.getLibelle(), d.getSymbole()))
                .toList();
    }

    @Override
    public List<RefItemDTO> pays() {
        return em.createQuery("select p from Pays p where p.actif = true order by p.nom", Pays.class)
                .getResultStream()
                .map(p -> new RefItemDTO(p.getIdPays(), p.getCode(), p.getNom(), p.getIndicatif()))
                .toList();
    }

    @Override
    public List<GeoDTO> regions(Long idPays) {
        String jpql = "select r from Region r"
                + (idPays != null ? " where r.pays.idPays = :p" : "") + " order by r.nom";
        var q = em.createQuery(jpql, Region.class);
        if (idPays != null) q.setParameter("p", idPays);
        return q.getResultStream()
                .map(r -> new GeoDTO(r.getIdRegion(), r.getNom(), r.getPays().getIdPays()))
                .toList();
    }

    @Override
    public List<GeoDTO> villes(Long idRegion) {
        String jpql = "select v from Ville v"
                + (idRegion != null ? " where v.region.idRegion = :r" : "") + " order by v.nom";
        var q = em.createQuery(jpql, Ville.class);
        if (idRegion != null) q.setParameter("r", idRegion);
        return q.getResultStream()
                .map(v -> new GeoDTO(v.getIdVille(), v.getNom(), v.getRegion().getIdRegion()))
                .toList();
    }
}
