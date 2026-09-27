package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.FavoriJpaEntity;

import java.util.List;
import java.util.Optional;

public interface FavoriJpaRepository extends JpaRepository<FavoriJpaEntity, Long> {

    List<FavoriJpaEntity> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    Optional<FavoriJpaEntity> findByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);

    Optional<FavoriJpaEntity> findByUtilisateurIdAndEspaceId(Long utilisateurId, Long espaceId);

    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);

    boolean existsByUtilisateurIdAndEspaceId(Long utilisateurId, Long espaceId);
}
