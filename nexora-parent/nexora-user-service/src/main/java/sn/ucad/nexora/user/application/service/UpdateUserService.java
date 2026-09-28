package sn.ucad.nexora.user.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.user.application.dto.request.UpdateUserRequest;
import sn.ucad.nexora.user.application.usecase.UpdateUserUseCase;
import sn.ucad.nexora.user.domain.entity.User;
import sn.ucad.nexora.user.domain.repository.UserRepository;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;

    public UpdateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User update(UUID accountId, UpdateUserRequest request) {

        if (request == null || request.getFirstName() == null || request.getFirstName().isBlank()
                || request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Prénom et nom sont obligatoires");
        }

        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
