package sn.ucad.nexora.catalogue.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.OffreJpaEntity;

public interface OffreJpaRepository extends JpaRepository<OffreJpaEntity, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE OffreJpaEntity o SET o.vueCount = o.vueCount + 1 WHERE o.id = :id")
    void incrementerVues(@Param("id") Long id);
}
