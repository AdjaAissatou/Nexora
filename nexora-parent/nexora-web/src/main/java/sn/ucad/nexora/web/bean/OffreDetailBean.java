package sn.ucad.nexora.web.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.RechercheApiClient;
import sn.ucad.nexora.web.dto.catalogue.OffreDetailResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code offre.xhtml} — la fiche détaillée d'une offre / d'un espace. */
@Named
@ViewScoped
public class OffreDetailBean implements Serializable {

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private transient RechercheApiClient rechercheApiClient;

    @Inject
    private SessionBean session;

    private Long id;
    private OffreDetailResponse offre;
    private String erreur;
    private boolean favori;
    /** Le même genre d'article dans d'autres espaces (recherche par photo, §11) ; chargé à la demande. */
    private java.util.List<sn.ucad.nexora.web.dto.catalogue.RechercheVisuelleDtos.Resultat> similaires;

    public void charger() {
        if (id == null) {
            erreur = "Offre introuvable.";
            return;
        }
        try {
            offre = catalogueApiClient.obtenir(id);
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        if (session.isConnecte()) {
            rechercheApiClient.enregistrerConsultation(session.getAccessToken(), id, null);
            try {
                favori = rechercheApiClient.listerFavoris(session.getAccessToken()).stream()
                        .anyMatch(f -> id.equals(f.offreId()));
            } catch (ApiException e) {
                favori = false;
            }
        }
    }

    public void basculerFavori() {
        if (!session.isConnecte() || offre == null) return;
        try {
            if (favori) {
                rechercheApiClient.supprimerFavori(session.getAccessToken(), id, null);
            } else {
                rechercheApiClient.ajouterFavori(session.getAccessToken(), id, null);
            }
            favori = !favori;
        } catch (ApiException e) {
            // Silencieux : un aller-retour favori raté n'empêche pas de consulter la fiche.
        }
    }

    public boolean isFavori() {
        return favori;
    }

    private java.util.List<sn.ucad.nexora.web.dto.catalogue.RechercheVisuelleDtos.Resultat> suggestions;

    /** « Vous pourriez aussi aimer » : articles voisins, pas le même article ailleurs (§11). */
    public java.util.List<sn.ucad.nexora.web.dto.catalogue.RechercheVisuelleDtos.Resultat> getSuggestions() {
        if (suggestions == null) suggestions = offre == null ? java.util.List.of() : catalogueApiClient.suggestions(java.util.List.of(offre.id()), 4);
        return suggestions;
    }

    public java.util.List<sn.ucad.nexora.web.dto.catalogue.RechercheVisuelleDtos.Resultat> getSimilaires() {
        if (similaires == null) similaires = offre == null ? java.util.List.of() : catalogueApiClient.similaires(offre.id(), 4);
        return similaires;
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
