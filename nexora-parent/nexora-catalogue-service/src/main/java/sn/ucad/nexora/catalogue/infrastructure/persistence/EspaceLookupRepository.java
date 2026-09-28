package sn.ucad.nexora.catalogue.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.EspaceLookupEntity;

public interface EspaceLookupRepository extends JpaRepository<EspaceLookupEntity, Long> {
}
