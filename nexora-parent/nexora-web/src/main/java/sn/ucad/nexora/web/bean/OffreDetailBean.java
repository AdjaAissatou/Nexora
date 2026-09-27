package sn.ucad.nexora.web.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.dto.catalogue.OffreDetailResponse;
import sn.ucad.nexora.web.error.ApiException;

/** Backing bean de {@code offre.xhtml} — la fiche détaillée d'une offre / d'un espace. */
@Named
@ViewScoped
public class OffreDetailBean implements Serializable {

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    private Long id;
    private OffreDetailResponse offre;
    private String erreur;

    public void charger() {
        if (id == null) {
            erreur = "Offre introuvable.";
            return;
        }
        try {
            offre = catalogueApiClient.obtenir(id);
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public boolean isTrouvee() {
        return offre != null;
    }

    public String getErreur() {
        return erreur;
    }

    public OffreDetailResponse getOffre() {
        return offre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
