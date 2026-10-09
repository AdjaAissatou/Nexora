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
        RechercheParams params = versParams(request);

        // Appel du repository
        List<Offre> offres = offreRepository.search(params);
        return page(request, offres);
    }

    /**
     * Identifiants des offres correspondant à une recherche, sans tenir compte des caractéristiques
     * choisies : sert aux facettes (§17), qui doivent proposer toutes les tailles, couleurs… possibles.
     */
    public List<Long> idsCorrespondants(OffreSearchRequest request, int max) {
        RechercheParams params = versParams(request);
        params.setValeurs(null);
        params.setTri("PERTINENCE");
        params.setPage(0);
        params.setTaille(max);
        return offreRepository.search(params).stream().map(Offre::getId).toList();
    }

    /** Identifiants d'une recherche avec tous ses filtres, dans l'ordre de la recherche (flux Découvrir, §18). */
    public List<Long> idsRecherche(OffreSearchRequest request, int max) {
        RechercheParams params = versParams(request);
        params.setPage(0);
        params.setTaille(max);
        return offreRepository.search(params).stream().map(Offre::getId).toList();
    }

    private RechercheParams versParams(OffreSearchRequest request) {
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
        params.setOuvertMaintenant(request.getOuvertMaintenant());
        params.setLat(request.getLat());
        params.setLng(request.getLng());
        params.setRayonKm(request.getRayonKm());
        params.setNoteMin(request.getNoteMin());
        params.setNeuf(request.getNeuf());
        params.setNegociable(request.getNegociable());
        params.setDomicile(request.getDomicile());
        params.setPopulaire(request.getPopulaire());
        params.setValeurs(request.getValeurs());
        params.setTri(request.getTri());
        params.setPage(request.getPage());
        params.setTaille(request.getTaille());
        return params;
    }

    private OffrePageResponse page(OffreSearchRequest request, List<Offre> offres) {
        // Mapper vers DTO résumé
        List<OffreSummaryResponse> items = offres.stream()
                .map(this::toSummary)
                .toList();

        // Vrai total (toutes pages) : sans lui, la première page se croyait la dernière
        long total = offres.size() < request.getTaille() && request.getPage() == 0
                ? offres.size() : offreRepository.count(versParams(request));

        return new OffrePageResponse(items, request.getPage(), request.getTaille(), total);
    }

    /** Résumés des offres visibles du public parmi ces identifiants (ordre non garanti). */
    public List<OffreSummaryResponse> resumesVisibles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        RechercheParams params = new RechercheParams();
        params.setIdsOffres(ids);
        params.setPage(0);
        params.setTaille(ids.size());
        return offreRepository.search(params).stream().map(this::toSummary).toList();
    }

    /** Offres visibles les plus récentes d'une catégorie (sous-catégories comprises). */
    public List<OffreSummaryResponse> recentesDeCategorie(Long idCategorie, int taille) {
        RechercheParams params = new RechercheParams();
        params.setIdCategorie(idCategorie);
        params.setTri("DATE_DESC");
        params.setPage(0);
        params.setTaille(taille);
        return offreRepository.search(params).stream().map(this::toSummary).toList();
    }

    /** Offres visibles les plus récentes d'un espace. */
    public List<OffreSummaryResponse> recentesDeEspace(Long idEspace, int taille) {
        RechercheParams params = new RechercheParams();
        params.setIdEspace(idEspace);
        params.setTri("DATE_DESC");
        params.setPage(0);
        params.setTaille(taille);
        return offreRepository.search(params).stream().map(this::toSummary).toList();
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
        r.setEspaceOuvertMaintenant(o.getEspaceOuvertMaintenant());
        r.setDistanceKm(o.getDistanceKm());
        r.setNombreVentes(o.getNombreVentes());
        r.setVueCount(o.getVueCount());
        r.setVuesDecouvrir(o.getVuesDecouvrir());
        r.setNombreJaime(o.getNombreJaime());
        r.setNombreFavoris(o.getNombreFavoris());
        r.setPopulaire(o.isPopulaire());
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
