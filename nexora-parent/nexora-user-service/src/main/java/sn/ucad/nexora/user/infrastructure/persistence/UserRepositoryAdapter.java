package sn.ucad.nexora.user.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.user.domain.entity.User;
import sn.ucad.nexora.user.domain.repository.UserRepository;
import sn.ucad.nexora.user.infrastructure.persistence.entity.UserJpaEntity;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository repository;

    public UserRepositoryAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId)
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {

        UserJpaEntity entity = toEntity(user);

        UserJpaEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private User toDomain(UserJpaEntity entity) {

        User user = new User();

        user.setId(entity.getId());
        user.setAccountId(entity.getAccountId());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());

        return user;
    }

    private UserJpaEntity toEntity(User user) {

        UserJpaEntity entity = new UserJpaEntity();

        entity.setId(user.getId());
        entity.setAccountId(user.getAccountId());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        return entity;
    }
}
