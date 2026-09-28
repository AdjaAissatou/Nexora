package sn.ucad.nexora.espace.domain.repository;

import java.util.List;
import sn.ucad.nexora.espace.domain.entity.TypeEspace;

public interface TypeEspaceRepository {
    List<TypeEspace> findAllActifs();
}
