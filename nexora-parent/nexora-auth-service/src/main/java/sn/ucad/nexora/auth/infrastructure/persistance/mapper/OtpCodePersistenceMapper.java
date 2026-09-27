package sn.ucad.nexora.auth.infrastructure.persistance.mapper;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.entity.OtpCode;
import sn.ucad.nexora.auth.infrastructure.persistance.entity.AccountEntity;
import sn.ucad.nexora.auth.infrastructure.persistance.entity.OtpCodeEntity;

@Component
public class OtpCodePersistenceMapper {

    public OtpCode toDomain(OtpCodeEntity entity) {

        if (entity == null) {
            return null;
        }

        OtpCode otp = new OtpCode();

        otp.setId(entity.getId());
        otp.setCode(entity.getCode());
        otp.setExpiresAt(entity.getExpiresAt());

        if (entity.isUsed()) {
            otp.use();
        }

        if (entity.getAccount() != null) {

            AccountEntity accountEntity = entity.getAccount();

            Account account = new Account();

            account.setId(accountEntity.getId());
            account.setFirstName(accountEntity.getFirstName());
            account.setLastName(accountEntity.getLastName());
            account.setEmail(accountEntity.getEmail());
            account.setPhone(accountEntity.getPhone());
            account.setBirthDate(accountEntity.getBirthDate());
            account.setPassword(accountEntity.getPassword());

            if (accountEntity.isEnabled()) {
                account.enable();
            } else {
                account.disable();
            }

            if (accountEntity.isVerified()) {
                account.verify();
            } else {
                account.unverify();
            }

            if (accountEntity.isLocked()) {
                account.lock();
            } else {
                account.unlock();
            }

            otp.setAccount(account);
        }

        return otp;
    }

    public OtpCodeEntity toEntity(OtpCode domain) {

        if (domain == null) {
            return null;
        }

        OtpCodeEntity entity = new OtpCodeEntity();

        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setUsed(domain.isUsed());

        if (domain.getAccount() != null) {

            AccountEntity account = new AccountEntity();

            account.setId(domain.getAccount().getId());

            entity.setAccount(account);
        }

        return entity;
    }
}