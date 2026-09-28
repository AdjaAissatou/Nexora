package sn.ucad.nexora.espace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.AdresseJpaEntity;

import java.util.List;
import java.util.Optional;

public interface AdresseLookupRepository extends JpaRepository<AdresseJpaEntity, Long> {
    List<AdresseJpaEntity> findByEspaceId(Long espaceId);

    default Optional<AdresseJpaEntity> findPrincipaleByEspaceId(Long espaceId) {
        return findByEspaceId(espaceId).stream()
                .filter(a -> Boolean.TRUE.equals(a.getPrincipale()))
                .findFirst()
                .or(() -> findByEspaceId(espaceId).stream().findFirst());
    }
}
