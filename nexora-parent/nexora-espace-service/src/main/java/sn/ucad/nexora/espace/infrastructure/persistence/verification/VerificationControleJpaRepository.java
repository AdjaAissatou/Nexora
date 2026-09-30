package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationControleJpaRepository extends JpaRepository<VerificationControleJpaEntity, Long> {
    List<VerificationControleJpaEntity> findByVerificationId(Long verificationId);
    Optional<VerificationControleJpaEntity> findByVerificationIdAndCode(Long verificationId, String code);
}
