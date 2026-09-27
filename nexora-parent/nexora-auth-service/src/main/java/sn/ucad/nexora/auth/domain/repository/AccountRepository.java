package sn.ucad.nexora.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import sn.ucad.nexora.auth.domain.entity.Account;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void delete(Account account);

}