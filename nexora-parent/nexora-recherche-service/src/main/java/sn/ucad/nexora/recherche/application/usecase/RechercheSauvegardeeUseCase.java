package sn.ucad.nexora.recherche.application.usecase;

import sn.ucad.nexora.recherche.application.dto.request.SauvegarderRechercheRequest;
import sn.ucad.nexora.recherche.application.dto.response.RechercheSauvegardeeResponse;
import java.util.List;

public interface RechercheSauvegardeeUseCase {
    RechercheSauvegardeeResponse sauvegarder(Long utilisateurId, SauvegarderRechercheRequest request);
    List<RechercheSauvegardeeResponse> lister(Long utilisateurId);
    void supprimer(Long utilisateurId, Long id);
}
