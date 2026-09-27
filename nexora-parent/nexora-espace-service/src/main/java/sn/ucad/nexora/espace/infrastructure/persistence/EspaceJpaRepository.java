package sn.ucad.nexora.espace.infrastructure.persistence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.EspaceJpaEntity;
public interface EspaceJpaRepository extends JpaRepository<EspaceJpaEntity,Long>{ List<EspaceJpaEntity> findByUtilisateurId(Long utilisateurId); }
