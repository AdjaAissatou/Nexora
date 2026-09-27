package sn.ucad.nexora.auth.infrastructure.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import sn.ucad.nexora.auth.application.port.outbound.UserServicePort;
import sn.ucad.nexora.auth.domain.entity.Account;

@Component
public class UserServiceAdapter implements UserServicePort {

    private final RestClient restClient;

    public UserServiceAdapter(
            @Value("${user.service.url}") String userServiceUrl) {

        this.restClient = RestClient.create(userServiceUrl);
    }

    @Override
    public void createUser(Account account) {

        restClient.post()
                .uri("/api/v1/users/internal")
                .body(new CreateUserRequest(
                        account.getId(),
                        account.getFirstName(),
                        account.getLastName(),
                        account.getPhone()
                ))
                .retrieve()
                .toBodilessEntity();
    }

    private static class CreateUserRequest {

        private final java.util.UUID accountId;
        private final String firstName;
        private final String lastName;
        private final String phone;

        public CreateUserRequest(
                java.util.UUID accountId,
                String firstName,
                String lastName,
                String phone) {

            this.accountId = accountId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
        }

        public java.util.UUID getAccountId() {
            return accountId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPhone() {
            return phone;
        }
    }
}