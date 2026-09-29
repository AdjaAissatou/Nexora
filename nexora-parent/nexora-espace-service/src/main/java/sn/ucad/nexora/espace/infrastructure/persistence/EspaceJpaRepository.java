package sn.ucad.nexora.espace.infrastructure.persistence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.EspaceJpaEntity;
public interface EspaceJpaRepository extends JpaRepository<EspaceJpaEntity,Long>{
    List<EspaceJpaEntity> findByUtilisateurId(Long utilisateurId);

    @Modifying
    @Transactional
    @Query("UPDATE EspaceJpaEntity e SET e.nombreVues = e.nombreVues + 1 WHERE e.id = :id")
    void incrementerVues(Long id);
}
