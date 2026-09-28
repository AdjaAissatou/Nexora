package sn.ucad.nexora.espace.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.TypeEspaceJpaEntity;

public interface TypeEspaceJpaRepository extends JpaRepository<TypeEspaceJpaEntity, Long> {
    List<TypeEspaceJpaEntity> findByActifTrueOrderByOrdreAffichageAsc();
}
