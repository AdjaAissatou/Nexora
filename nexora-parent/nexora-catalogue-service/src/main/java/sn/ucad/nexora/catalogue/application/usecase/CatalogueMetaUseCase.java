package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.response.AttributResponse;
import sn.ucad.nexora.catalogue.application.dto.response.CategorieResponse;
import sn.ucad.nexora.catalogue.application.dto.response.TypeOffreResponse;

import java.util.List;

/** Navigation du catalogue (catégories, types d'offre, attributs) pour le formulaire de création d'offre. */
public interface CatalogueMetaUseCase {
    List<CategorieResponse> categoriesRacines();
    List<CategorieResponse> categoriesRacines(Long idTypeEspace);
    List<CategorieResponse> sousCategories(Long idCategorie);
    List<TypeOffreResponse> typesOffre(Long idCategorie);
    List<AttributResponse> attributs(Long idCategorie);
}
