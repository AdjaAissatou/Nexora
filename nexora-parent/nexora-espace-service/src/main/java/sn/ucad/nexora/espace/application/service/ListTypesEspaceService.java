package sn.ucad.nexora.espace.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.espace.application.usecase.ListTypesEspaceUseCase;
import sn.ucad.nexora.espace.domain.entity.TypeEspace;
import sn.ucad.nexora.espace.domain.repository.TypeEspaceRepository;

@Service
public class ListTypesEspaceService implements ListTypesEspaceUseCase {

    private final TypeEspaceRepository repository;

    public ListTypesEspaceService(TypeEspaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TypeEspace> lister() {
        return repository.findAllActifs();
    }
}
