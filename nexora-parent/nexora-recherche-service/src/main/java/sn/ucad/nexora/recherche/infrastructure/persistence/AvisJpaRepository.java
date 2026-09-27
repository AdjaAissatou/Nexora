package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.AvisJpaEntity;

import java.util.List;

public interface AvisJpaRepository extends JpaRepository<AvisJpaEntity, Long> {

    List<AvisJpaEntity> findByOffreIdOrderByDateCreationDesc(Long offreId);

    List<AvisJpaEntity> findByEspaceIdOrderByDateCreationDesc(Long espaceId);

    List<AvisJpaEntity> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);
}
