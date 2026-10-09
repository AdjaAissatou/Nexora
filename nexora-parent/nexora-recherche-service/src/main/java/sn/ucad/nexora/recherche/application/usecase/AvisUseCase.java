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

    // §22 : modifier son avis, mes avis, avis reçus par un espace et réponse publique du professionnel
    AvisResponse modifier(Long utilisateurId, Long avisId, sn.ucad.nexora.recherche.application.dto.response.AvisDtos.ModifierAvisRequest request);
    List<sn.ucad.nexora.recherche.application.dto.response.AvisDtos.MonAvisDetail> mesAvis(Long utilisateurId);
    sn.ucad.nexora.recherche.application.dto.response.AvisDtos.AvisRecus avisRecus(Long utilisateurId, Long espaceId);
    sn.ucad.nexora.recherche.application.dto.response.AvisDtos.AvisRecu repondre(Long utilisateurId, Long avisId, String texte);
    sn.ucad.nexora.recherche.application.dto.response.AvisDtos.AvisRecu retirerReponse(Long utilisateurId, Long avisId);
}
