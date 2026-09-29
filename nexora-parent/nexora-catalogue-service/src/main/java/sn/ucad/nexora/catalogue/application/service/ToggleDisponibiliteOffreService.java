package sn.ucad.nexora.catalogue.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.usecase.ToggleDisponibiliteOffreUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.EspaceLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.EspaceLookupEntity;

import java.util.UUID;

/** Bascule rapide disponible/épuisé d'une offre, sans repasser par tout le formulaire d'édition. */
@Service
public class ToggleDisponibiliteOffreService implements ToggleDisponibiliteOffreUseCase {

    private final EntityManager em;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final EspaceLookupRepository espaceRepository;

    public ToggleDisponibiliteOffreService(EntityManager em, UtilisateurLookupRepository utilisateurRepository,
                                            EspaceLookupRepository espaceRepository) {
        this.em = em;
        this.utilisateurRepository = utilisateurRepository;
        this.espaceRepository = espaceRepository;
    }

    @Override
    @Transactional
    public void basculer(UUID accountId, Long idOffre, boolean disponible) {
        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }

        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        Long idEspace = idEspaceDeLoffre(idOffre);
        EspaceLookupEntity espace = espaceRepository.findById(idEspace)
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));
        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette offre");
        }

        Query q = em.createNativeQuery(
                "UPDATE offre SET disponible = :disponible, date_modification = NOW() WHERE id_offre = :id");
        q.setParameter("disponible", disponible);
        q.setParameter("id", idOffre);
        q.executeUpdate();
    }

    private Long idEspaceDeLoffre(Long idOffre) {
        Query q = em.createNativeQuery("SELECT id_espace FROM offre WHERE id_offre = :id");
        q.setParameter("id", idOffre);
        try {
            return ((Number) q.getSingleResult()).longValue();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Offre introuvable");
        }
    }
}
