package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.HistoriqueConsultationJpaEntity;

import java.util.List;

public interface HistoriqueConsultationJpaRepository
        extends JpaRepository<HistoriqueConsultationJpaEntity, Long> {

    List<HistoriqueConsultationJpaEntity> findByUtilisateurIdOrderByDateConsultationDesc(Long utilisateurId);

    @Modifying @Transactional
    @Query("DELETE FROM HistoriqueConsultationJpaEntity c WHERE c.utilisateurId = :uid")
    void deleteByUtilisateurId(@Param("uid") Long utilisateurId);
}
