package sn.ucad.nexora.auth.infrastructure.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import sn.ucad.nexora.auth.domain.entity.Role;
import sn.ucad.nexora.auth.domain.repository.RoleRepository;
import sn.ucad.nexora.auth.infrastructure.persistance.mapper.RolePersistenceMapper;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final SpringDataRoleRepository repository;

    private final RolePersistenceMapper mapper;

    public RoleRepositoryAdapter(SpringDataRoleRepository repository, RolePersistenceMapper mapper) {
		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
    public Role save(Role role) {

        return mapper.toDomain(
                repository.save(
                        mapper.toEntity(role)
                )
        );

    }

    @Override
    public Optional<Role> findById(UUID id) {

        return repository.findById(id)
                .map(mapper::toDomain);

    }

    @Override
    public Optional<Role> findByCode(String code) {

        return repository.findByCode(code)
                .map(mapper::toDomain);

    }

    @Override
    public List<Role> findAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();

    }

}