package com.nexora.repository;

import com.nexora.domain.space.EspaceProfessionnel;
import jakarta.ejb.Stateless;

import java.util.List;
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

    /** Espaces en attente de validation par un controleur qualite. */
    public List<EspaceProfessionnel> findNonVerifies() {
        return em().createQuery(
                        "select e from EspaceProfessionnel e where e.verifie = false and e.actif = true "
                                + "order by e.createdAt desc",
                        EspaceProfessionnel.class)
                .getResultList();
    }

    /**
     * Nombre d'espaces possedes par un utilisateur. Sert a deriver le statut
     * "fournisseur" : un utilisateur est fournisseur des lors qu'il possede
     * au moins un espace professionnel (aucun role dedie n'est necessaire).
     */
    public long countByProprietaire(Long idProprietaire) {
        return em().createQuery(
                        "select count(e) from EspaceProfessionnel e "
                                + "where e.proprietaire.idUtilisateur = :id", Long.class)
                .setParameter("id", idProprietaire)
                .getSingleResult();
    }

    /** Nombre d'offres publiees rattachees a un espace. */
    public long countOffres(Long idEspace) {
        return em().createQuery(
                        "select count(o) from Offre o where o.espace.idEspace = :id",
                        Long.class)
                .setParameter("id", idEspace)
                .getSingleResult();
    }
}
