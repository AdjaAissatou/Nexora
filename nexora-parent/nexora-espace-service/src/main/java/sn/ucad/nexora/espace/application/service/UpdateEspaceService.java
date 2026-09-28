package sn.ucad.nexora.espace.application.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.request.UpdateEspaceRequest;
import sn.ucad.nexora.espace.application.usecase.UpdateEspaceUseCase;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
import sn.ucad.nexora.espace.domain.repository.EspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.AdresseLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.GeoQueryRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.AdresseJpaEntity;

@Service
public class UpdateEspaceService implements UpdateEspaceUseCase {

    private final EspaceRepository espaceRepository;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final GeoQueryRepository geoRepository;
    private final AdresseLookupRepository adresseRepository;

    public UpdateEspaceService(EspaceRepository espaceRepository, UtilisateurLookupRepository utilisateurRepository,
                                GeoQueryRepository geoRepository, AdresseLookupRepository adresseRepository) {
        this.espaceRepository = espaceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.geoRepository = geoRepository;
        this.adresseRepository = adresseRepository;
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

        GeoQueryRepository.GeoNoms noms = geoRepository.verifierEtResoudre(
                request.getIdRegion(), request.getIdDepartement(), request.getIdCommune());

        espace.setNom(request.getNom().trim());
        espace.setSlogan(request.getSlogan());
        espace.setDescription(request.getDescription());
        espace.setTelephone(request.getTelephone());
        espace.setTelephoneSecondaire(request.getTelephoneSecondaire());
        espace.setEmail(request.getEmail());
        espace.setSiteWeb(request.getSiteWeb());
        espace.setLogo(request.getLogo());
        espace.setCouverture(request.getCouverture());
        if (request.getOuvert() != null) {
            espace.setOuvert(request.getOuvert());
        }
        espace.setDateModification(LocalDateTime.now());

        EspaceProfessionnel saved = espaceRepository.save(espace);

        AdresseJpaEntity adresse = adresseRepository.findPrincipaleByEspaceId(espaceId).orElseGet(AdresseJpaEntity::new);
        adresse.setEspaceId(espaceId);
        adresse.setPays("Sénégal");
        adresse.setRegion(noms.region());
        adresse.setDepartement(noms.departement());
        adresse.setCommune(noms.commune());
        adresse.setQuartier(request.getQuartier().trim());
        adresse.setAdresseComplete(request.getAdresseComplete());
        adresse.setPrincipale(true);
        adresseRepository.save(adresse);

        return saved;
    }
}
