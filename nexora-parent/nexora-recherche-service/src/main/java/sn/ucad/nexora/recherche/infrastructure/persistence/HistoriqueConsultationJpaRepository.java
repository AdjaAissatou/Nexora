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

    // Bornée : l'historique n'a pas de suppression automatique, une consultation ajoute
    // toujours une nouvelle ligne (même offre revisitée = nouvelle ligne) — Top100 évite
    // de renvoyer un historique illimité à l'affichage.
    List<HistoriqueConsultationJpaEntity> findTop100ByUtilisateurIdOrderByDateConsultationDesc(Long utilisateurId);

    @Modifying @Transactional
    @Query("DELETE FROM HistoriqueConsultationJpaEntity c WHERE c.utilisateurId = :uid")
    void deleteByUtilisateurId(@Param("uid") Long utilisateurId);
}
