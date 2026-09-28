package sn.ucad.nexora.catalogue.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse.LocalisationResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse.ProduitResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse.PromotionResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse.ServiceResponse;
import sn.ucad.nexora.catalogue.application.usecase.GetOffreUseCase;
import sn.ucad.nexora.catalogue.domain.entity.Offre;
import sn.ucad.nexora.catalogue.domain.entity.Produit;
import sn.ucad.nexora.catalogue.domain.repository.OffreRepository;

@Service
public class GetOffreService implements GetOffreUseCase {

    private final OffreRepository offreRepository;

    public GetOffreService(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    @Override
    public OffreDetailResponse get(Long id) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable : " + id));

        // Incrémenter les vues de façon asynchrone (best-effort)
        try {
            offreRepository.incrementerVues(id);
        } catch (Exception ignored) {
            // Ne pas bloquer la consultation pour une erreur de compteur
        }

        return toDetail(offre);
    }

    private OffreDetailResponse toDetail(Offre o) {
        OffreDetailResponse r = new OffreDetailResponse();

        // Offre
        r.setId(o.getId());
        r.setTitre(o.getTitre());
        r.setDescription(o.getDescription());
        r.setPrix(o.getPrix());
        r.setAncienPrix(o.getAncienPrix());
        r.setNegociable(o.isNegociable());
        r.setDisponible(o.isDisponible());
        r.setEstCommandable(o.isEstCommandable());
        r.setEstReservable(o.isEstReservable());
        r.setStockable(o.isStockable());
        r.setQuantiteDisponible(o.getQuantiteDisponible());
        r.setVueCount(o.getVueCount());
        r.setStatut(o.getStatut());
        r.setDatePublication(o.getDatePublication());

        // Catégorie
        r.setCategorieId(o.getCategorieId());
        r.setCategorie(o.getCategorie());

        // Espace
        r.setEspaceId(o.getEspaceId());
        r.setEspaceNom(o.getEspaceNom());
        r.setEspaceLogo(o.getEspaceLogo());
        r.setEspaceTelephone(o.getEspaceTelephone());
        r.setEspaceCertifie(o.isEspaceCertifie());
        r.setEspaceVerifie(o.isEspaceVerifie());
        r.setEspaceOuvert(o.isEspaceOuvert());
        r.setEspaceNoteMoyenne(o.getEspaceNoteMoyenne());
        r.setEspaceNombreAvis(o.getEspaceNombreAvis());
        r.setTypeEspace(o.getTypeEspace());

        // Localisation
        if (o.getLatitude() != null && o.getLongitude() != null) {
            LocalisationResponse loc = new LocalisationResponse();
            loc.setPays(o.getPays());
            loc.setRegion(o.getRegion());
            loc.setDepartement(o.getDepartement());
            loc.setCommune(o.getCommune());
            loc.setQuartier(o.getQuartier());
            loc.setAdresseComplete(o.getAdresseComplete());
            loc.setLatitude(o.getLatitude());
            loc.setLongitude(o.getLongitude());
            r.setLocalisation(loc);
        }

        // Promotion
        if (o.getPromotionNom() != null) {
            PromotionResponse promo = new PromotionResponse();
            promo.setNom(o.getPromotionNom());
            promo.setTypeReduction(o.getTypeReduction());
            promo.setValeur(o.getValeurReduction());
            r.setPromotion(promo);
        }

        // Images
        r.setImages(o.getImages());
        r.setImagePrincipale(o.getImagePrincipale());

        // Produit
        if (o.getProduit() != null) {
            Produit p = o.getProduit();
            ProduitResponse pr = new ProduitResponse();
            pr.setMarque(p.getMarque());
            pr.setModele(p.getModele());
            pr.setReference(p.getReference());
            pr.setQuantiteStock(p.getQuantiteStock());
            pr.setPoids(p.getPoids());
            pr.setGarantie(p.getGarantie());
            pr.setNeuf(p.isNeuf());
            r.setProduit(pr);
        }

        // Service
        if (o.getService() != null) {
            sn.ucad.nexora.catalogue.domain.entity.Service s = o.getService();
            ServiceResponse sr = new ServiceResponse();
            sr.setDureeEstimee(s.getDureeEstimee());
            sr.setInterventionDomicile(s.isInterventionDomicile());
            sr.setInterventionDistance(s.isInterventionDistance());
            sr.setDelaiReponse(s.getDelaiReponse());
            sr.setReservation(s.isReservation());
            sr.setUrgence(s.isUrgence());
            r.setService(sr);
        }

        // Tags
        r.setTags(o.getTags());

        return r;
    }
}
