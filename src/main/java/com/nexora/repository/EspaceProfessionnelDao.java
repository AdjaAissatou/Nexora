package com.nexora.repository;

import com.nexora.domain.space.EspaceProfessionnel;
import jakarta.ejb.Stateless;

import java.util.Optional;

/** DAO de l'entite {@link EspaceProfessionnel}. */
@Stateless
public class EspaceProfessionnelDao extends GenericDao<EspaceProfessionnel, Long> {

    public Optional<EspaceProfessionnel> findBySlug(String slug) {
        return em().createQuery(
                        "select e from EspaceProfessionnel e where e.slug = :slug",
                        EspaceProfessionnel.class)
                .setParameter("slug", slug)
                .getResultStream()
                .findFirst();
    }
}
