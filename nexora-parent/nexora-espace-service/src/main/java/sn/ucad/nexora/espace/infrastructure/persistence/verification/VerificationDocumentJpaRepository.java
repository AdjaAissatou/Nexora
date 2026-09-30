package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationDocumentJpaRepository extends JpaRepository<VerificationDocumentJpaEntity, Long> {
    List<VerificationDocumentJpaEntity> findByVerificationIdOrderByIdAsc(Long verificationId);
}
