package sn.ucad.nexora.espace.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.request.UpdateEspaceRequest;
import sn.ucad.nexora.espace.application.service.verification.VerificationModificationService;
import sn.ucad.nexora.espace.application.service.verification.VerificationModificationService.InformationsVerifiees;
import sn.ucad.nexora.espace.application.usecase.UpdateEspaceUseCase;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
import sn.ucad.nexora.espace.domain.repository.EspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.AdresseLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.GeoQueryRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.PhotoEspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.AdresseJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.PhotoEspaceJpaEntity;

@Service
public class UpdateEspaceService implements UpdateEspaceUseCase {

    private final EspaceRepository espaceRepository;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final GeoQueryRepository geoRepository;
    private final AdresseLookupRepository adresseRepository;
    private final PhotoEspaceRepository photoRepository;
    private final VerificationModificationService verificationModification;

    public UpdateEspaceService(EspaceRepository espaceRepository, UtilisateurLookupRepository utilisateurRepository,
                                GeoQueryRepository geoRepository, AdresseLookupRepository adresseRepository,
                                PhotoEspaceRepository photoRepository,
                                VerificationModificationService verificationModification) {
        this.espaceRepository = espaceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.geoRepository = geoRepository;
        this.adresseRepository = adresseRepository;
        this.photoRepository = photoRepository;
        this.verificationModification = verificationModification;
    }

    @Override
    @Transactional
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

        AdresseJpaEntity adresseAvant = adresseRepository.findPrincipaleByEspaceId(espaceId).orElse(null);
        InformationsVerifiees avant = new InformationsVerifiees(espace.getNom(), espace.getTelephone(),
                espace.getNumeroNinea(), espace.getNumeroRccm(), espace.getRegistreCommerce(),
                adresseAvant != null ? adresseAvant.getRegion() : null,
                adresseAvant != null ? adresseAvant.getDepartement() : null,
                adresseAvant != null ? adresseAvant.getCommune() : null,
                adresseAvant != null ? adresseAvant.getQuartier() : null,
                adresseAvant != null ? adresseAvant.getAdresseComplete() : null);
        InformationsVerifiees apres = new InformationsVerifiees(request.getNom(), request.getTelephone(),
                request.getNumeroNinea(), request.getNumeroRccm(), request.getRegistreCommerce(),
                noms.region(), noms.departement(), noms.commune(), request.getQuartier(), request.getAdresseComplete());
        List<String> champsVerifiesModifies = avant.differences(apres);

        espace.setNom(request.getNom().trim());
        espace.setSlogan(request.getSlogan());
        espace.setDescription(request.getDescription());
        espace.setTelephone(request.getTelephone());
        espace.setTelephoneSecondaire(request.getTelephoneSecondaire());
        espace.setEmail(request.getEmail());
        espace.setSiteWeb(request.getSiteWeb());
        espace.setLogo(request.getLogo());
        espace.setCouverture(request.getCouverture());
        espace.setRegistreCommerce(request.getRegistreCommerce());
        espace.setNumeroNinea(request.getNumeroNinea());
        espace.setNumeroRccm(request.getNumeroRccm());
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
        adresse.setLatitude(request.getLatitude());
        adresse.setLongitude(request.getLongitude());
        adresse.setPrincipale(true);
        adresseRepository.save(adresse);

        remplacerPhotos(espaceId, request.getPhotos());

        // §8.4.4 : une information vérifiée modifiée retire le badge (ou est signalée à l'agent
        // si une demande est en cours d'examen).
        if (verificationModification.surModification(espaceId, utilisateurId, champsVerifiesModifies)) {
            saved.setVerifie(false);
            saved.setDateVerification(null);
        }

        return saved;
    }

    private void remplacerPhotos(Long espaceId, List<String> urls) {
        photoRepository.deleteByEspaceId(espaceId);
        if (urls == null) return;
        int ordre = 0;
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            PhotoEspaceJpaEntity photo = new PhotoEspaceJpaEntity();
            photo.setEspaceId(espaceId);
            photo.setUrl(url.trim());
            photo.setOrdreAffichage(ordre++);
            photoRepository.save(photo);
        }
    }
}
