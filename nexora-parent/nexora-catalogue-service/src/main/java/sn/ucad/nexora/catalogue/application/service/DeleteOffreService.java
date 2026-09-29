package sn.ucad.nexora.catalogue.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.usecase.DeleteOffreUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.EspaceLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.EspaceLookupEntity;

import java.util.UUID;

/**
 * Suppression d'une offre par le propriétaire de son espace. Les tables dépendantes (produit,
 * service, offre_attribut, image, promotion, offre_tag, ressource, disponibilite) n'ont pas de
 * ON DELETE CASCADE sur id_offre, donc on les vide explicitement avant de supprimer la ligne offre.
 */
@Service
public class DeleteOffreService implements DeleteOffreUseCase {

    private final EntityManager em;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final EspaceLookupRepository espaceRepository;

    public DeleteOffreService(EntityManager em, UtilisateurLookupRepository utilisateurRepository,
                               EspaceLookupRepository espaceRepository) {
        this.em = em;
        this.utilisateurRepository = utilisateurRepository;
        this.espaceRepository = espaceRepository;
    }

    @Override
    @Transactional
    public void supprimer(UUID accountId, Long idOffre) {
        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }

        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        Long idEspace = idEspaceDeLoffre(idOffre);
        EspaceLookupEntity espace = espaceRepository.findById(idEspace)
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));
        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer cette offre");
        }

        for (String table : new String[] {
                "produit", "service", "offre_attribut", "image", "promotion", "offre_tag", "ressource", "disponibilite"
        }) {
            Query q = em.createNativeQuery("DELETE FROM " + table + " WHERE id_offre = :id");
            q.setParameter("id", idOffre);
            q.executeUpdate();
        }

        Query supprimerOffre = em.createNativeQuery("DELETE FROM offre WHERE id_offre = :id");
        supprimerOffre.setParameter("id", idOffre).executeUpdate();
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
