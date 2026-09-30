package sn.ucad.nexora.catalogue.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.response.OffrePageResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreSummaryResponse;
import sn.ucad.nexora.catalogue.application.usecase.RechercherOffresUseCase;
import sn.ucad.nexora.catalogue.domain.entity.Offre;
import sn.ucad.nexora.catalogue.domain.repository.OffreRepository;
import sn.ucad.nexora.catalogue.domain.repository.RechercheParams;
import sn.ucad.nexora.catalogue.infrastructure.persistence.moderation.ModerationOffreRepository;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;

import java.util.List;
import java.util.UUID;

@Service
public class RechercherOffresService implements RechercherOffresUseCase {

    /** Au-delà, la liste de gestion est tronquée (un espace en a rarement autant). */
    private static final int TAILLE_GESTION = 200;

    private final OffreRepository offreRepository;
    private final ModerationOffreRepository moderation;

    public RechercherOffresService(OffreRepository offreRepository, ModerationOffreRepository moderation) {
        this.offreRepository = offreRepository;
        this.moderation = moderation;
    }

    @Override
    public OffrePageResponse gestion(UUID accountId, Long idEspace) {
        Long utilisateur = accountId == null ? null : moderation.utilisateurId(accountId).orElse(null);
        Long proprietaire = moderation.proprietaireEspace(idEspace).orElseThrow(() -> new ResourceNotFoundException("Espace introuvable"));
        if (!proprietaire.equals(utilisateur)) throw new UnauthorizedException("Cet espace ne vous appartient pas");
        RechercheParams params = new RechercheParams();
        params.setGestion(true);
        params.setIdEspace(idEspace);
        params.setTri("DATE_DESC");
        params.setPage(0);
        params.setTaille(TAILLE_GESTION);
        List<OffreSummaryResponse> items = offreRepository.search(params).stream().map(this::toSummary).toList();
        return new OffrePageResponse(items, 0, TAILLE_GESTION, items.size());
    }

    @Override
    public OffrePageResponse rechercher(OffreSearchRequest request) {

        // Construire les paramètres de recherche domaine
        RechercheParams params = new RechercheParams();
        params.setQ(request.getQ());
        params.setCategorie(request.getCategorie());
        params.setIdCategorie(request.getIdCategorie());
        params.setTypeEspace(request.getTypeEspace());
        params.setCommune(request.getCommune());
        params.setRegion(request.getRegion());
        params.setIdEspace(request.getIdEspace());
        params.setPrixMin(request.getPrixMin());
        params.setPrixMax(request.getPrixMax());
        params.setEstProduit(request.getEstProduit());
        params.setAvecPromotion(request.getAvecPromotion());
        params.setEspaceVerifie(request.getEspaceVerifie());
        params.setTri(request.getTri());
        params.setPage(request.getPage());
        params.setTaille(request.getTaille());

        // Appel du repository
        List<Offre> offres = offreRepository.search(params);

        // Mapper vers DTO résumé
        List<OffreSummaryResponse> items = offres.stream()
                .map(this::toSummary)
                .toList();

        // La pagination réelle nécessiterait un count séparé ;
        // ici on expose le total partiel (suffisant pour les listes de résultats).
        long total = items.size() + (long) request.getPage() * request.getTaille();

        return new OffrePageResponse(items, request.getPage(), request.getTaille(), total);
    }

    private OffreSummaryResponse toSummary(Offre o) {
        OffreSummaryResponse r = new OffreSummaryResponse();
        r.setId(o.getId());
        r.setTitre(o.getTitre());
        r.setPrix(o.getPrix());
        r.setAncienPrix(o.getAncienPrix());
        r.setImagePrincipale(o.getImagePrincipale());
        r.setDisponible(o.isDisponible());
        r.setNegociable(o.isNegociable());
        r.setStatut(o.getStatut());
        r.setMotifModeration(o.getMotifModeration());
        r.setEspaceId(o.getEspaceId());
        r.setEspaceNom(o.getEspaceNom());
        r.setEspaceLogo(o.getEspaceLogo());
        r.setEspaceCertifie(o.isEspaceCertifie());
        r.setEspaceVerifie(o.isEspaceVerifie());
        r.setEspaceNoteMoyenne(o.getEspaceNoteMoyenne());
        r.setEspaceNombreAvis(o.getEspaceNombreAvis());
        r.setTypeEspace(o.getTypeEspace());
        r.setCategorie(o.getCategorie());
        r.setCommune(o.getCommune());
        r.setRegion(o.getRegion());
        r.setLatitude(o.getLatitude());
        r.setLongitude(o.getLongitude());
        r.setPromotionNom(o.getPromotionNom());
        r.setTypeReduction(o.getTypeReduction());
        r.setValeurReduction(o.getValeurReduction());
        return r;
    }
}
