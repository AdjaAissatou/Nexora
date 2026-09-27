package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.SignalementJpaEntity;

import java.util.List;

public interface SignalementJpaRepository extends JpaRepository<SignalementJpaEntity, Long> {

    List<SignalementJpaEntity> findByUtilisateurId(Long utilisateurId);

    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);
}
