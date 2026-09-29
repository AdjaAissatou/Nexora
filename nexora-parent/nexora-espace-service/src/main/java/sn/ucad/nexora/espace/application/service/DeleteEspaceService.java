package sn.ucad.nexora.espace.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.usecase.DeleteEspaceUseCase;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
import sn.ucad.nexora.espace.domain.repository.EspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;

@Service
public class DeleteEspaceService implements DeleteEspaceUseCase {

    private final EspaceRepository espaceRepository;
    private final UtilisateurLookupRepository utilisateurRepository;

    public DeleteEspaceService(EspaceRepository espaceRepository, UtilisateurLookupRepository utilisateurRepository) {
        this.espaceRepository = espaceRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public void supprimer(UUID accountId, Long espaceId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }

        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        EspaceProfessionnel espace = espaceRepository.findById(espaceId)
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));

        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cet espace");
        }

        espaceRepository.deleteById(espaceId);
    }
}
