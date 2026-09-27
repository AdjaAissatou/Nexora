package sn.ucad.nexora.recherche.application.usecase;

import sn.ucad.nexora.recherche.application.dto.request.SignalerRequest;

public interface SignalementUseCase {
    void signaler(Long utilisateurId, SignalerRequest request);
}
