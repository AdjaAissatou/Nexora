package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import sn.ucad.nexora.auth.domain.entity.Permission;
import sn.ucad.nexora.auth.domain.repository.PermissionRepository;
import sn.ucad.nexora.auth.infrastructure.persistance.mapper.PermissionPersistenceMapper;

@Repository
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final SpringDataPermissionRepository repository;

    private final PermissionPersistenceMapper mapper;

    public PermissionRepositoryAdapter(SpringDataPermissionRepository repository, PermissionPersistenceMapper mapper) {
		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
    public Permission save(Permission permission) {

        return mapper.toDomain(
                repository.save(
                        mapper.toEntity(permission)
                )
        );

    }

    @Override
    public Optional<Permission> findById(UUID id) {

        return repository.findById(id)
                .map(mapper::toDomain);

    }

    @Override
    public Optional<Permission> findByCode(String code) {

        return repository.findByCode(code)
                .map(mapper::toDomain);

    }

    @Override
    public List<Permission> findAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();

    }

}