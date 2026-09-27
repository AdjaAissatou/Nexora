package sn.ucad.nexora.user.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.user.application.dto.request.CreateUserRequest;
import sn.ucad.nexora.user.application.usecase.CreateUserUseCase;
import sn.ucad.nexora.user.domain.entity.User;
import sn.ucad.nexora.user.domain.repository.UserRepository;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(CreateUserRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Les informations utilisateur sont obligatoires"
            );
        }

        if (request.getAccountId() == null) {
            throw new IllegalArgumentException(
                    "Account ID obligatoire"
            );
        }

        // Empêcher la création de plusieurs profils
        // pour le même compte
        if (userRepository
                .findByAccountId(request.getAccountId())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Un profil utilisateur existe déjà pour ce compte"
            );
        }

        User user = new User();


        user.setAccountId(
                request.getAccountId()
        );

        user.setFirstName(
                request.getFirstName()
        );

        user.setLastName(
                request.getLastName()
        );

        user.setPhone(
                request.getPhone()
        );

        LocalDateTime now =
                LocalDateTime.now();

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return userRepository.save(user);
    }
}