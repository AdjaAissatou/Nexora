package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.ucad.nexora.auth.infrastructure.persistance.entity.OtpCodeEntity;

public interface SpringDataOtpCodeRepository
		extends JpaRepository<OtpCodeEntity, UUID> {
	
	Optional<OtpCodeEntity> findByCode(String code);
	
	Optional<OtpCodeEntity> findByAccount_Id(UUID accountId);
	
	Optional<OtpCodeEntity> findByAccount_IdAndCode(UUID accountId, String code);

}