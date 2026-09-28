package sn.ucad.nexora.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import sn.ucad.nexora.user.application.usecase.GetUserUseCase;
import sn.ucad.nexora.user.domain.entity.User;
import sn.ucad.nexora.user.domain.repository.UserRepository;

@Service
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Utilisateur introuvable"
                        )
                );
    }

    @Override
    public User getByAccountId(UUID accountId) {

        return userRepository.findByAccountId(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Profil utilisateur introuvable"
                        )
                );
    }
}