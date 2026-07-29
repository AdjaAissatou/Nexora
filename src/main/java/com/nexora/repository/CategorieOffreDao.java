package com.nexora.repository;

import com.nexora.domain.catalog.CategorieOffre;
import jakarta.ejb.Stateless;

import java.util.List;

/** DAO de l'entite {@link CategorieOffre}. */
@Stateless
public class CategorieOffreDao extends GenericDao<CategorieOffre, Long> {

    /** Categories racines visibles, triees pour l'affichage. */
    public List<CategorieOffre> findVisiblesRacines() {
        return em().createQuery(
                        "select c from CategorieOffre c where c.visible = true and c.parent is null "
                                + "order by c.ordreAffichage asc, c.nom asc",
                        CategorieOffre.class)
                .getResultList();
    }
}
