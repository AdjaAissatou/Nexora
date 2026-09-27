package sn.ucad.nexora.recherche.application.usecase;

import sn.ucad.nexora.recherche.application.dto.request.EnregistrerRechercheRequest;
import sn.ucad.nexora.recherche.application.dto.response.HistoriqueRechercheResponse;
import java.util.List;

public interface HistoriqueRechercheUseCase {
    /** Enregistre une recherche effectuée (appelé automatiquement). */
    void enregistrer(Long utilisateurId, EnregistrerRechercheRequest request);
    /** Liste les N dernières recherches de l'utilisateur. */
    List<HistoriqueRechercheResponse> lister(Long utilisateurId);
    /** Supprime tout l'historique de recherche. */
    void effacer(Long utilisateurId);
}
