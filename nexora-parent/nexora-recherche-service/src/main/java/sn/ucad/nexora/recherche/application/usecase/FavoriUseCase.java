package sn.ucad.nexora.recherche.application.usecase;

import sn.ucad.nexora.recherche.application.dto.request.AjouterFavoriRequest;
import sn.ucad.nexora.recherche.application.dto.response.FavoriResponse;
import java.util.List;

public interface FavoriUseCase {
    FavoriResponse ajouter(Long utilisateurId, AjouterFavoriRequest request);
    void supprimer(Long utilisateurId, Long offreId, Long espaceId);
    List<FavoriResponse> lister(Long utilisateurId);
}
