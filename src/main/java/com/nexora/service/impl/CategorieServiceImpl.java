package com.nexora.service.impl;

import com.nexora.domain.attribute.Attribut;
import com.nexora.dto.CategorieDTO;
import com.nexora.dto.ref.AttributDTO;
import com.nexora.dto.ref.ValeurAttributDTO;
import com.nexora.repository.CategorieOffreDao;
import com.nexora.service.CategorieService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/** Implementation du service des categories. */
@Stateless
public class CategorieServiceImpl implements CategorieService {

    @Inject
    private CategorieOffreDao categorieDao;

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    @Override
    public List<CategorieDTO> categoriesRacines() {
        return categorieDao.findVisiblesRacines().stream().map(CategorieDTO::new).toList();
    }

    @Override
    public List<CategorieDTO> toutesCategories() {
        return em.createQuery(
                        "select c from CategorieOffre c where c.visible = true order by c.nom",
                        com.nexora.domain.catalog.CategorieOffre.class)
                .getResultStream().map(CategorieDTO::new).toList();
    }

    @Override
    public List<AttributDTO> attributsDeCategorie(Long idCategorie) {
        List<Attribut> attributs = em.createQuery(
                        "select a from Attribut a where a.categorie.idCategorie = :id and a.actif = true "
                                + "order by a.ordreAffichage nulls last, a.nom", Attribut.class)
                .setParameter("id", idCategorie)
                .getResultList();
        return attributs.stream().map(a -> {
            List<ValeurAttributDTO> valeurs = a.getValeursPossibles().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getActif()))
                    .sorted((x, y) -> Integer.compare(x.getOrdre(), y.getOrdre()))
                    .map(v -> new ValeurAttributDTO(v.getIdValeurAttribut(), v.getValeur()))
                    .toList();
            return new AttributDTO(a, valeurs);
        }).toList();
    }
}
