package sn.ucad.nexora.espace.infrastructure.persistence;

import java.util.List;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.espace.domain.entity.TypeEspace;
import sn.ucad.nexora.espace.domain.repository.TypeEspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.TypeEspaceJpaEntity;

@Component
public class TypeEspaceRepositoryAdapter implements TypeEspaceRepository {

    private final TypeEspaceJpaRepository repository;

    public TypeEspaceRepositoryAdapter(TypeEspaceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TypeEspace> findAllActifs() {
        return repository.findByActifTrueOrderByOrdreAffichageAsc().stream().map(this::toDomain).toList();
    }

    private TypeEspace toDomain(TypeEspaceJpaEntity e) {
        TypeEspace d = new TypeEspace();
        d.setId(e.getId());
        d.setNom(e.getNom());
        d.setDescription(e.getDescription());
        d.setIcone(e.getIcone());
        d.setCouleur(e.getCouleur());
        d.setOrdreAffichage(e.getOrdreAffichage());
        return d;
    }
}
