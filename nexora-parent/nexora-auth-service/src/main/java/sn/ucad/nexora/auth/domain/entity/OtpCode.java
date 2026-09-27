package sn.ucad.nexora.auth.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import sn.ucad.nexora.common.domain.BaseDomainEntity;

public class OtpCode extends BaseDomainEntity{


    private String code;

    private LocalDateTime expiresAt;

    private boolean used;

    private Account account;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void use() {
        this.used = true;
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}