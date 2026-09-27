package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import sn.ucad.nexora.auth.domain.entity.OtpCode;
import sn.ucad.nexora.auth.domain.repository.OtpCodeRepository;
import sn.ucad.nexora.auth.infrastructure.persistance.mapper.OtpCodePersistenceMapper;

@Repository
public class OtpCodeRepositoryAdapter implements OtpCodeRepository {

    private final SpringDataOtpCodeRepository repository;
    private final OtpCodePersistenceMapper mapper;

    public OtpCodeRepositoryAdapter(SpringDataOtpCodeRepository repository,
                                    OtpCodePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OtpCode save(OtpCode otp) {
        return mapper.toDomain(
                repository.save(
                        mapper.toEntity(otp)
                )
        );
    }

    @Override
    public Optional<OtpCode> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OtpCode> findByCode(String code) {
        return repository.findByCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OtpCode> findByAccountId(UUID accountId) {
        return repository.findByAccount_Id(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<OtpCode> findByAccountIdAndCode(UUID accountId, String code) {
        return repository.findByAccount_IdAndCode(accountId, code)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(OtpCode otp) {
        repository.delete(
                mapper.toEntity(otp)
        );
    }

}