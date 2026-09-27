package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.RechercheSauvegardeeJpaEntity;

import java.util.List;

public interface RechercheSauvegardeeJpaRepository
        extends JpaRepository<RechercheSauvegardeeJpaEntity, Long> {

    List<RechercheSauvegardeeJpaEntity> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);
}
