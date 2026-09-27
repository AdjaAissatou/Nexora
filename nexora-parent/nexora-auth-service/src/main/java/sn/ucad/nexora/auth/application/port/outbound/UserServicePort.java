package sn.ucad.nexora.auth.application.port.outbound;

import sn.ucad.nexora.auth.domain.entity.Account;

public interface UserServicePort {

    void createUser(Account account);
}