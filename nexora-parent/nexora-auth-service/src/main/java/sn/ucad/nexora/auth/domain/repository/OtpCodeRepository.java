package sn.ucad.nexora.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import sn.ucad.nexora.auth.domain.entity.OtpCode;

public interface OtpCodeRepository {

    OtpCode save(OtpCode otp);

    Optional<OtpCode> findById(UUID id);

    Optional<OtpCode> findByCode(String code);

    Optional<OtpCode> findByAccountId(UUID accountId);

    Optional<OtpCode> findByAccountIdAndCode(UUID accountId, String code);

    void delete(OtpCode otp);
}