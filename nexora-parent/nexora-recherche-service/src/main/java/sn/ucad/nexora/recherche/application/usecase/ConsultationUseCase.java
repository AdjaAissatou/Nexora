package sn.ucad.nexora.recherche.application.usecase;

import java.util.List;
import sn.ucad.nexora.recherche.application.dto.request.EnregistrerConsultationRequest;
import sn.ucad.nexora.recherche.application.dto.response.HistoriqueConsultationResponse;

public interface ConsultationUseCase {
    void enregistrer(Long utilisateurId, EnregistrerConsultationRequest request);
    List<HistoriqueConsultationResponse> lister(Long utilisateurId);
    void effacer(Long utilisateurId);
}
