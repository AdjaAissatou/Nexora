package com.nexora.repository;

import com.nexora.domain.dispute.Litige;
import jakarta.ejb.Stateless;

import java.util.List;

/** DAO de l'entite {@link Litige}. */
@Stateless
public class LitigeDao extends GenericDao<Litige, Long> {

    /** Litiges non encore resolus (a arbitrer). */
    public List<Litige> findOuverts() {
        return em().createQuery(
                        "select l from Litige l where l.statut is null or lower(l.statut) <> 'resolu' "
                                + "order by l.dateOuverture desc",
                        Litige.class)
                .getResultList();
    }
}
