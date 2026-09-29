package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sn.ucad.nexora.web.client.AvisApiClient;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.RechercheApiClient;
import sn.ucad.nexora.web.dto.catalogue.OffreSummaryResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.recherche.AvisResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code espace.xhtml} — fiche publique d'un espace professionnel. */
@Named
@ViewScoped
public class EspaceDetailBean implements Serializable {

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private transient AvisApiClient avisApiClient;

    @Inject
    private transient RechercheApiClient rechercheApiClient;

    @Inject
    private SessionBean session;

    private Long id;
    private EspaceResponse espace;
    private List<OffreSummaryResponse> offres;
    private List<AvisResponse> avis;
    private String erreur;
    private boolean trouve;
    private boolean favori;

    public void charger() {
        if (id == null) return;
        try {
            espace = espaceApiClient.obtenir(id);
            trouve = true;
            espaceApiClient.enregistrerVue(id);
        } catch (ApiException e) {
            erreur = e.getMessage();
            trouve = false;
            return;
        }
        try {
            offres = catalogueApiClient.rechercher(CritereRecherche.parEspace(id)).contenu();
        } catch (ApiException e) {
            offres = List.of();
        }
        try {
            avis = avisApiClient.parEspace(id);
        } catch (ApiException e) {
            avis = List.of();
        }
        if (session.isConnecte()) {
            rechercheApiClient.enregistrerConsultation(session.getAccessToken(), null, id);
            try {
                favori = rechercheApiClient.listerFavoris(session.getAccessToken()).stream()
                        .anyMatch(f -> id.equals(f.espaceId()));
            } catch (ApiException e) {
                favori = false;
            }
        }
    }

    public void basculerFavori() {
        if (!session.isConnecte() || espace == null) return;
        try {
            if (favori) {
                rechercheApiClient.supprimerFavori(session.getAccessToken(), null, id);
            } else {
                rechercheApiClient.ajouterFavori(session.getAccessToken(), null, id);
            }
            favori = !favori;
        } catch (ApiException e) {
            // Silencieux : un aller-retour favori raté n'empêche pas de consulter la fiche.
        }
    }

    public boolean isFavori() { return favori; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EspaceResponse getEspace() { return espace; }
    public List<OffreSummaryResponse> getOffres() { return offres; }
    public List<AvisResponse> getAvis() { return avis; }
    public String getErreur() { return erreur; }
    public boolean isTrouve() { return trouve; }
}
