package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import org.primefaces.event.FileUploadEvent;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.UserApiClient;
import sn.ucad.nexora.web.config.ImageUploadService;
import sn.ucad.nexora.web.dto.catalogue.OffreSummaryResponse;
import sn.ucad.nexora.web.dto.espace.CommuneResponse;
import sn.ucad.nexora.web.dto.espace.DepartementResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.espace.RegionResponse;
import sn.ucad.nexora.web.dto.espace.TypeEspaceResponse;
import sn.ucad.nexora.web.dto.espace.UpdateEspaceRequest;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;
import sn.ucad.nexora.web.util.VocabulaireOffres;

/** Backing bean de {@code mon-espace.xhtml} — gestion de l'espace (ou des espaces) professionnel du compte connecté. */
@Named
@ViewScoped
public class MonEspaceBean implements Serializable {

    @Inject
    private transient UserApiClient userApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private SessionBean session;

    @Inject
    private VerificationEspaceBean verification;

    @Inject
    private HorairesEspaceBean horaires;

    private List<EspaceResponse> mesEspaces;
    private EspaceResponse espace;
    private List<OffreSummaryResponse> mesOffres;
    private String erreur;
    private VocabulaireOffres.Vocabulaire vocabulaireOffres = VocabulaireOffres.pour(null);

    // Champs modifiables de l'espace
    private String espaceNom;
    private String espaceSlogan;
    private String espaceDescription;
    private String espaceTelephone;
    private String espaceTelephoneSecondaire;
    private String espaceEmail;
    private String espaceSiteWeb;
    private boolean espaceOuvert;

    // Localisation — toujours choisie en cascade, jamais saisie librement pour région/département/commune.
    private List<RegionResponse> regions;
    private Long idRegion;
    private List<DepartementResponse> departements;
    private Long idDepartement;
    private List<CommuneResponse> communes;
    private Long idCommune;
    private String quartier;
    private String adresseComplete;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;

    private String espaceLogo;
    private String espaceCouverture;
    private String espaceRegistreCommerce;
    private String espaceNumeroNinea;
    private String espaceNumeroRccm;
    private String photosTexte;

    @PostConstruct
    public void charger() {
        try {
            // Simple garde-fou : s'assurer que le profil existe côté user-service avant de
            // continuer. L'édition du profil elle-même vit désormais dans MonCompteBean.
            userApiClient.obtenir(session.getAccessToken(), session.getCompte().id());
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        try {
            mesEspaces = espaceApiClient.mesEspaces(session.getAccessToken());
            // Un compte peut posséder plusieurs espaces ; celui à éditer est choisi via ?id=,
            // sinon le premier par défaut. mesEspaces() ne renvoie pas l'adresse (uniquement
            // GET /{id}) : on recharge le détail complet de l'espace sélectionné.
            Long idSelectionne = idDepuisParametre();
            EspaceResponse selectionne = mesEspaces.stream()
                    .filter(e -> e.id().equals(idSelectionne))
                    .findFirst()
                    .orElse(mesEspaces.isEmpty() ? null : mesEspaces.get(0));
            espace = selectionne == null ? null : espaceApiClient.obtenir(session.getAccessToken(), selectionne.id());
            remplirChampsEspace();
            vocabulaireOffres = VocabulaireOffres.pour(nomTypeEspaceDe(espace));
            mesOffres = espace == null ? List.of()
                    : catalogueApiClient.gestion(session.getAccessToken(), espace.id()).contenu();
            if (espace != null) verification.initialiser(espace.id());
            if (espace != null) horaires.initialiser(espace.id());
        } catch (ApiException e) {
            // Pas encore de profil utilisateur exploitable côté espace-service : pas une erreur bloquante,
            // l'utilisateur voit simplement la proposition de créer son espace.
            mesEspaces = List.of();
            espace = null;
        }
    }

    private String nomTypeEspaceDe(EspaceResponse e) {
        if (e == null || e.typeEspaceId() == null) return null;
        try {
            return espaceApiClient.listerTypes().stream()
                    .filter(t -> t.id().equals(e.typeEspaceId()))
                    .map(TypeEspaceResponse::nom)
                    .findFirst().orElse(null);
        } catch (ApiException ex) {
            return null;
        }
    }

    private Long idDepuisParametre() {
        String brut = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        try {
            return brut == null ? null : Long.valueOf(brut);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void remplirChampsEspace() {
        if (espace == null) return;
        espaceNom = espace.nom();
        espaceSlogan = espace.slogan();
        espaceDescription = espace.description();
        espaceTelephone = espace.telephone();
        espaceTelephoneSecondaire = espace.telephoneSecondaire();
        espaceEmail = espace.email();
        espaceSiteWeb = espace.siteWeb();
        espaceOuvert = espace.ouvert();
        quartier = espace.quartier();
        adresseComplete = espace.adresseComplete();
        latitude = espace.latitude();
        longitude = espace.longitude();
        espaceLogo = espace.logo();
        espaceCouverture = espace.couverture();
        espaceRegistreCommerce = espace.registreCommerce();
        espaceNumeroNinea = espace.numeroNinea();
        espaceNumeroRccm = espace.numeroRccm();
        photosTexte = espace.photos() == null ? null : String.join("\n", espace.photos());

        regions = espaceApiClient.regions();
        idRegion = regions.stream().filter(r -> r.nom().equals(espace.region())).map(RegionResponse::id).findFirst().orElse(null);
        if (idRegion != null) {
            departements = espaceApiClient.departements(idRegion);
            idDepartement = departements.stream().filter(d -> d.nom().equals(espace.departement()))
                    .map(DepartementResponse::id).findFirst().orElse(null);
        }
        if (idDepartement != null) {
            communes = espaceApiClient.communes(idDepartement);
            idCommune = communes.stream().filter(c -> c.nom().equals(espace.commune()))
                    .map(CommuneResponse::id).findFirst().orElse(null);
        }
    }

    public void onRegionChange() {
        departements = null;
        idDepartement = null;
        communes = null;
        idCommune = null;
        if (idRegion == null) return;
        departements = espaceApiClient.departements(idRegion);
    }

    public void onDepartementChange() {
        communes = null;
        idCommune = null;
        if (idDepartement == null) return;
        communes = espaceApiClient.communes(idDepartement);
    }

    public String enregistrerEspace() {
        boolean etaitVerifie = espace.verifie();
        try {
            espace = espaceApiClient.mettreAJour(
                    session.getAccessToken(),
                    espace.id(),
                    new UpdateEspaceRequest(
                            espaceNom,
                            espaceSlogan,
                            espaceDescription,
                            espaceTelephone,
                            espaceTelephoneSecondaire,
                            espaceEmail,
                            espaceSiteWeb,
                            espaceOuvert,
                            idRegion,
                            idDepartement,
                            idCommune,
                            quartier,
                            adresseComplete,
                            latitude,
                            longitude,
                            espaceLogo,
                            espaceCouverture,
                            espaceRegistreCommerce,
                            espaceNumeroNinea,
                            espaceNumeroRccm,
                            urlsDepuisTexte(photosTexte)));
            remplirChampsEspace();
            mesEspaces = mesEspaces.stream().map(e -> e.id().equals(espace.id()) ? espace : e).toList();
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_INFO, "Espace mis à jour.", null));
            if (etaitVerifie && !espace.verifie()) {
                // §8.4.4 : une information vérifiée a changé, espace-service a retiré le badge.
                FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_WARN,
                        "Badge Vérifié retiré",
                        "Vous avez modifié une information vérifiée. Vous pouvez redemander la vérification dans l'onglet Vérification."));
            }
            verification.recharger();
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Mise à jour impossible", e.getMessage()));
        }
        return null;
    }

    public String supprimerEspace() {
        try {
            espaceApiClient.supprimer(session.getAccessToken(), espace.id());
            // Supprimer son dernier espace retire le rôle FOURNISSEUR : on recharge les rôles,
            // et on renvoie vers Mon compte puisque « Mon espace » disparaît de la navigation.
            session.rafraichir();
            return session.isFournisseur() ? "mon-espace?faces-redirect=true" : "mon-compte?faces-redirect=true";
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Suppression impossible", e.getMessage()));
            return null;
        }
    }

    public void supprimerOffre(Long idOffre) {
        try {
            catalogueApiClient.supprimerOffre(session.getAccessToken(), idOffre);
            mesOffres = catalogueApiClient.gestion(session.getAccessToken(), espace.id()).contenu();
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Suppression impossible", e.getMessage()));
        }
    }

    public void basculerDisponibiliteOffre(Long idOffre, boolean disponible) {
        try {
            catalogueApiClient.basculerDisponibiliteOffre(session.getAccessToken(), idOffre, disponible);
            mesOffres = catalogueApiClient.gestion(session.getAccessToken(), espace.id()).contenu();
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Mise à jour impossible", e.getMessage()));
        }
    }

    private static List<String> urlsDepuisTexte(String texte) {
        if (texte == null || texte.isBlank()) return List.of();
        return texte.lines().map(String::trim).filter(l -> !l.isBlank()).toList();
    }

    /**
     * Nommé sans "is + deux majuscules" à dessein : {@code isAEspace()} exposerait la propriété EL
     * "AEspace" (majuscule conservée) et non "aEspace", par la règle de décapitalisation de
     * {@link java.beans.Introspector} — {@code #{monEspaceBean.aEspace}} échouerait silencieusement.
     */
    public boolean isPossedeEspace() {
        return espace != null;
    }

    public List<EspaceResponse> getMesEspaces() {
        return mesEspaces;
    }

    public boolean isPlusieursEspaces() {
        return mesEspaces != null && mesEspaces.size() > 1;
    }

    public List<OffreSummaryResponse> getMesOffres() {
        return mesOffres;
    }

    public String getLibelleSectionOffres() {
        return vocabulaireOffres.section();
    }

    public String getLibelleAjouterOffre() {
        return vocabulaireOffres.ajouter();
    }

    public String getLibelleOffresPluriel() {
        return vocabulaireOffres.pluriel();
    }

    public int getNombreOffresDisponibles() {
        return mesOffres == null ? 0 : (int) mesOffres.stream().filter(OffreSummaryResponse::disponible).count();
    }

    public EspaceResponse getEspace() {
        return espace;
    }

    public String getErreur() {
        return erreur;
    }

    public String getEspaceNom() {
        return espaceNom;
    }

    public void setEspaceNom(String espaceNom) {
        this.espaceNom = espaceNom;
    }

    public String getEspaceSlogan() {
        return espaceSlogan;
    }

    public void setEspaceSlogan(String espaceSlogan) {
        this.espaceSlogan = espaceSlogan;
    }

    public String getEspaceDescription() {
        return espaceDescription;
    }

    public void setEspaceDescription(String espaceDescription) {
        this.espaceDescription = espaceDescription;
    }

    public String getEspaceTelephone() {
        return espaceTelephone;
    }

    public void setEspaceTelephone(String espaceTelephone) {
        this.espaceTelephone = espaceTelephone;
    }

    public String getEspaceTelephoneSecondaire() {
        return espaceTelephoneSecondaire;
    }

    public void setEspaceTelephoneSecondaire(String espaceTelephoneSecondaire) {
        this.espaceTelephoneSecondaire = espaceTelephoneSecondaire;
    }

    public String getEspaceEmail() {
        return espaceEmail;
    }

    public void setEspaceEmail(String espaceEmail) {
        this.espaceEmail = espaceEmail;
    }

    public String getEspaceSiteWeb() {
        return espaceSiteWeb;
    }

    public void setEspaceSiteWeb(String espaceSiteWeb) {
        this.espaceSiteWeb = espaceSiteWeb;
    }

    public boolean isEspaceOuvert() {
        return espaceOuvert;
    }

    public void setEspaceOuvert(boolean espaceOuvert) {
        this.espaceOuvert = espaceOuvert;
    }

    public List<RegionResponse> getRegions() {
        return regions;
    }

    public Long getIdRegion() {
        return idRegion;
    }

    public void setIdRegion(Long idRegion) {
        this.idRegion = idRegion;
    }

    public List<DepartementResponse> getDepartements() {
        return departements;
    }

    public Long getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(Long idDepartement) {
        this.idDepartement = idDepartement;
    }

    public List<CommuneResponse> getCommunes() {
        return communes;
    }

    public Long getIdCommune() {
        return idCommune;
    }

    public void setIdCommune(Long idCommune) {
        this.idCommune = idCommune;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getAdresseComplete() {
        return adresseComplete;
    }

    public void setAdresseComplete(String adresseComplete) {
        this.adresseComplete = adresseComplete;
    }

    public java.math.BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(java.math.BigDecimal latitude) {
        this.latitude = latitude;
    }

    public java.math.BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(java.math.BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getEspaceLogo() {
        return espaceLogo;
    }

    public void setEspaceLogo(String espaceLogo) {
        this.espaceLogo = espaceLogo;
    }

    public String getEspaceCouverture() {
        return espaceCouverture;
    }

    public void setEspaceCouverture(String espaceCouverture) {
        this.espaceCouverture = espaceCouverture;
    }

    public String getEspaceRegistreCommerce() {
        return espaceRegistreCommerce;
    }

    public void setEspaceRegistreCommerce(String espaceRegistreCommerce) {
        this.espaceRegistreCommerce = espaceRegistreCommerce;
    }

    public String getEspaceNumeroNinea() {
        return espaceNumeroNinea;
    }

    public void setEspaceNumeroNinea(String espaceNumeroNinea) {
        this.espaceNumeroNinea = espaceNumeroNinea;
    }

    public String getEspaceNumeroRccm() {
        return espaceNumeroRccm;
    }

    public void setEspaceNumeroRccm(String espaceNumeroRccm) {
        this.espaceNumeroRccm = espaceNumeroRccm;
    }

    public String getPhotosTexte() {
        return photosTexte;
    }

    public void setPhotosTexte(String photosTexte) {
        this.photosTexte = photosTexte;
    }

    public void uploaderLogo(FileUploadEvent event) {
        espaceLogo = enregistrerUpload(event);
    }

    public void uploaderCouverture(FileUploadEvent event) {
        espaceCouverture = enregistrerUpload(event);
    }

    public void uploaderPhoto(FileUploadEvent event) {
        String url = enregistrerUpload(event);
        if (url == null) return;
        photosTexte = (photosTexte == null || photosTexte.isBlank()) ? url : photosTexte + "\n" + url;
    }

    private String enregistrerUpload(FileUploadEvent event) {
        try {
            return ImageUploadService.enregistrer(event.getFile().getInputStream(), event.getFile().getFileName());
        } catch (IOException | IllegalArgumentException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_ERROR, "Envoi impossible", e.getMessage()));
            return null;
        }
    }
}
