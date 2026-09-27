package sn.ucad.nexora.recherche.application.usecase;

import sn.ucad.nexora.recherche.application.dto.request.EnregistrerConsultationRequest;

public interface ConsultationUseCase {
    void enregistrer(Long utilisateurId, EnregistrerConsultationRequest request);
    void effacer(Long utilisateurId);
}
