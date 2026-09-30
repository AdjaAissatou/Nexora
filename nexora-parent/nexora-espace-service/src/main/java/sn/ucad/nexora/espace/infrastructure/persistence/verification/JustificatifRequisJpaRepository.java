package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JustificatifRequisJpaRepository extends JpaRepository<JustificatifRequisJpaEntity, Long> {

    /** Règles qui s'appliquent à un type d'espace : les règles communes (NULL) et les siennes. */
    @Query("SELECT r FROM JustificatifRequisJpaEntity r WHERE r.typeEspaceId IS NULL OR r.typeEspaceId = :typeEspaceId ORDER BY r.id")
    List<JustificatifRequisJpaEntity> pourTypeEspace(@Param("typeEspaceId") Long typeEspaceId);
}
