package sn.ucad.nexora.recherche.application.usecase;

import sn.ucad.nexora.recherche.application.dto.request.PublierAvisRequest;
import sn.ucad.nexora.recherche.application.dto.response.AvisResponse;
import java.util.List;

public interface AvisUseCase {
    AvisResponse publier(Long utilisateurId, PublierAvisRequest request);
    List<AvisResponse> listerParOffre(Long offreId);
    List<AvisResponse> listerParEspace(Long espaceId);
    void supprimer(Long utilisateurId, Long avisId);
    /** Un avis visible (non masqué), pour le formulaire de signalement. */
    AvisResponse visible(Long avisId);
}
