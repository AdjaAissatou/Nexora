package sn.ucad.nexora.catalogue.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.AttributResponse;
import sn.ucad.nexora.catalogue.application.dto.response.CategorieResponse;
import sn.ucad.nexora.catalogue.application.dto.response.TypeOffreResponse;
import sn.ucad.nexora.catalogue.application.usecase.CatalogueMetaUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.CategorieQueryRepository;

import java.util.List;

@Service
public class CatalogueMetaService implements CatalogueMetaUseCase {

    private final CategorieQueryRepository repository;

    public CatalogueMetaService(CategorieQueryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CategorieResponse> categoriesRacines() {
        return repository.racines();
    }

    @Override
    public List<CategorieResponse> categoriesRacines(Long idTypeEspace) {
        if (idTypeEspace == null) return repository.racines();
        // Si aucun domaine n'est rattaché à ce type d'espace (nouveau type d'espace pas
        // encore mappé), mieux vaut tout montrer que de bloquer le professionnel.
        List<CategorieResponse> filtrees = repository.racinesParTypeEspace(idTypeEspace);
        return filtrees.isEmpty() ? repository.racines() : filtrees;
    }

    @Override
    public List<CategorieResponse> sousCategories(Long idCategorie) {
        return repository.enfants(idCategorie);
    }

    @Override
    public List<TypeOffreResponse> typesOffre(Long idCategorie) {
        return repository.typesOffre(idCategorie);
    }

    @Override
    public List<AttributResponse> attributs(Long idCategorie) {
        return repository.attributs(idCategorie);
    }
}
