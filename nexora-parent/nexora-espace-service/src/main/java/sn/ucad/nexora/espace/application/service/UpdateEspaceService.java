package sn.ucad.nexora.espace.application.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.request.UpdateEspaceRequest;
import sn.ucad.nexora.espace.application.usecase.UpdateEspaceUseCase;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
import sn.ucad.nexora.espace.domain.repository.EspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;

@Service
public class UpdateEspaceService implements UpdateEspaceUseCase {

    private final EspaceRepository espaceRepository;
    private final UtilisateurLookupRepository utilisateurRepository;

    public UpdateEspaceService(EspaceRepository espaceRepository, UtilisateurLookupRepository utilisateurRepository) {
        this.espaceRepository = espaceRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public EspaceProfessionnel update(UUID accountId, Long espaceId, UpdateEspaceRequest request) {

        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }

        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        EspaceProfessionnel espace = espaceRepository.findById(espaceId)
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));

        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cet espace");
        }

        espace.setNom(request.getNom().trim());
        espace.setSlogan(request.getSlogan());
        espace.setDescription(request.getDescription());
        espace.setTelephone(request.getTelephone());
        espace.setTelephoneSecondaire(request.getTelephoneSecondaire());
        espace.setEmail(request.getEmail());
        espace.setSiteWeb(request.getSiteWeb());
        if (request.getOuvert() != null) {
            espace.setOuvert(request.getOuvert());
        }
        espace.setDateModification(LocalDateTime.now());

        return espaceRepository.save(espace);
    }
}
