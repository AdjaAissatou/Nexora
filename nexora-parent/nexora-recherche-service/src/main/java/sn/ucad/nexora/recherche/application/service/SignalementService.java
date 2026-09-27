package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.SignalerRequest;
import sn.ucad.nexora.recherche.application.usecase.SignalementUseCase;
import sn.ucad.nexora.recherche.domain.entity.Signalement;
import sn.ucad.nexora.recherche.domain.repository.SignalementRepository;

import java.time.LocalDateTime;

@Service
public class SignalementService implements SignalementUseCase {

    private final SignalementRepository repository;

    public SignalementService(SignalementRepository repository) {
        this.repository = repository;
    }

    @Override
    public void signaler(Long utilisateurId, SignalerRequest request) {
        if (request.getOffreId() == null && request.getEspaceId() == null) {
            throw new IllegalArgumentException("offreId ou espaceId est requis");
        }
        Signalement s = new Signalement();
        s.setUtilisateurId(utilisateurId);
        s.setOffreId(request.getOffreId());
        s.setEspaceId(request.getEspaceId());
        s.setMotif(request.getMotif());
        s.setDescription(request.getDescription());
        s.setStatut("EN_ATTENTE");
        s.setDateCreation(LocalDateTime.now());
        repository.save(s);
    }
}
