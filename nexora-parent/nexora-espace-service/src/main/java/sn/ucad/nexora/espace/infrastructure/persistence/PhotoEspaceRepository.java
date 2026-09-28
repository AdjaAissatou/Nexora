package sn.ucad.nexora.espace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.PhotoEspaceJpaEntity;

import java.util.List;

public interface PhotoEspaceRepository extends JpaRepository<PhotoEspaceJpaEntity, Long> {
    List<PhotoEspaceJpaEntity> findByEspaceIdOrderByOrdreAffichageAsc(Long espaceId);
    void deleteByEspaceId(Long espaceId);
}
