package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.SignalerRequest;
import sn.ucad.nexora.recherche.application.service.moderation.SignalementAdminService;
import sn.ucad.nexora.recherche.application.usecase.SignalementUseCase;

/** Dépôt d'un signalement : les règles sont dans {@link SignalementAdminService#signaler}. */
@Service
public class SignalementService implements SignalementUseCase {

    private final SignalementAdminService service;

    public SignalementService(SignalementAdminService service) {
        this.service = service;
    }

    @Override
    public void signaler(Long utilisateurId, SignalerRequest request) {
        service.signaler(utilisateurId, request.getEspaceId(), request.getOffreId(), request.getAvisId(),
                request.getMotif(), request.getDescription());
    }
}
