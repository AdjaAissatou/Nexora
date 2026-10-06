package sn.ucad.nexora.espace.infrastructure.persistence;
import java.time.LocalDateTime;
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
    @Query("UPDATE EspaceJpaEntity e SET e.nombreVues = COALESCE(e.nombreVues, 0) + 1 WHERE e.id = :id")
    void incrementerVues(Long id);

    /**
     * Projection de l'état de vérification (badge, filtre « vérifiés uniquement »). Écrite
     * uniquement par le processus de vérification, dans la transaction de la décision.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE EspaceJpaEntity e SET e.verifie = :verifie, e.dateVerification = :date WHERE e.id = :id")
    void majVerification(Long id, Boolean verifie, LocalDateTime date);
}
