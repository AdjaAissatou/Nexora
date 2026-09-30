package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationEvenementJpaRepository extends JpaRepository<VerificationEvenementJpaEntity, Long> {
    List<VerificationEvenementJpaEntity> findByVerificationIdOrderByDateEvenementAscIdAsc(Long verificationId);
}
