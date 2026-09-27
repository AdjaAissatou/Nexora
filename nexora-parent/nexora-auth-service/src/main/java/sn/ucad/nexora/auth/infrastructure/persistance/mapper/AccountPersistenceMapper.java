package sn.ucad.nexora.auth.infrastructure.persistance.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.infrastructure.persistance.entity.AccountEntity;

@Component
public class AccountPersistenceMapper {

    private final RolePersistenceMapper roleMapper;

    public AccountPersistenceMapper(RolePersistenceMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public Account toDomain(AccountEntity entity) {

        if (entity == null)
            return null;

        Account account = new Account();

        account.setId(entity.getId());
        account.setFirstName(entity.getFirstName());
        account.setLastName(entity.getLastName());
        account.setEmail(entity.getEmail());
        account.setPhone(entity.getPhone());
        account.setBirthDate(entity.getBirthDate());
        account.changePassword(entity.getPassword());

        if (entity.isEnabled())
            account.enable();
        else
            account.disable();

        if (entity.isVerified())
            account.verify();
        else
            account.unverify();

        if (entity.isLocked())
            account.lock();
        else
            account.unlock();

        account.setRoles(
                entity.getRoles()
                        .stream()
                        .map(roleMapper::toDomain)
                        .collect(Collectors.toSet()));

        return account;
    }

    public AccountEntity toEntity(Account domain) {

        if (domain == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();

        entity.setId(domain.getId());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setBirthDate(domain.getBirthDate());
        entity.setPassword(domain.getPassword());

        entity.setEnabled(domain.isEnabled());
        entity.setVerified(domain.isVerified());
        entity.setLocked(domain.isLocked());

        entity.setRoles(
                domain.getRoles()
                        .stream()
                        .map(roleMapper::toEntity)
                        .collect(Collectors.toSet())
        );

        return entity;
    }

}