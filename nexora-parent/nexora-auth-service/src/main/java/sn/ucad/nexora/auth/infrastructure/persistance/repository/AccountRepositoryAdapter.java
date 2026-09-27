package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.auth.infrastructure.persistance.mapper.AccountPersistenceMapper;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;

    private final AccountPersistenceMapper mapper;
    public AccountRepositoryAdapter(
            SpringDataAccountRepository repository,
            AccountPersistenceMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    public Account save(Account account) {
        return mapper.toDomain(
                repository.save(
                        mapper.toEntity(account)
                )
        );
    }

    @Override
    public Optional<Account> findById(UUID id) {

        return repository.findById(id)
                .map(mapper::toDomain);

    }

    @Override
    public Optional<Account> findByEmail(String email) {

        return repository.findByEmail(email)
                .map(mapper::toDomain);

    }

    @Override
    public Optional<Account> findByPhone(String phone) {

        return repository.findByPhone(phone)
                .map(mapper::toDomain);

    }

    @Override
    public boolean existsByEmail(String email) {

        return repository.existsByEmail(email);

    }

    @Override
    public boolean existsByPhone(String phone) {

        return repository.existsByPhone(phone);

    }

    @Override
    public void delete(Account account) {

        repository.delete(
                mapper.toEntity(account)
        );

    }

}