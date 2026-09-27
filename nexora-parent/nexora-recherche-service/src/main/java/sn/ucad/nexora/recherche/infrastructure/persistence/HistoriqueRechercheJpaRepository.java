package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.HistoriqueRechercheJpaEntity;

import java.util.List;

public interface HistoriqueRechercheJpaRepository
        extends JpaRepository<HistoriqueRechercheJpaEntity, Long> {

    List<HistoriqueRechercheJpaEntity> findByUtilisateurIdOrderByDateRechercheDesc(Long utilisateurId);

    @Modifying @Transactional
    @Query("DELETE FROM HistoriqueRechercheJpaEntity h WHERE h.utilisateurId = :uid")
    void deleteByUtilisateurId(@Param("uid") Long utilisateurId);
}
