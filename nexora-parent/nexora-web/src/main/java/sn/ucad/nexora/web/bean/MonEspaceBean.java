package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.client.UserApiClient;
import sn.ucad.nexora.web.dto.espace.CommuneResponse;
import sn.ucad.nexora.web.dto.espace.DepartementResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.dto.espace.RegionResponse;
import sn.ucad.nexora.web.dto.espace.UpdateEspaceRequest;
import sn.ucad.nexora.web.dto.user.UpdateUserRequest;
import sn.ucad.nexora.web.dto.user.UserResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code mon-espace.xhtml} — profil du compte connecté et son espace professionnel. */
@Named
@ViewScoped
public class MonEspaceBean implements Serializable {

    @Inject
    private transient UserApiClient userApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private SessionBean session;

    private UserResponse profil;
    private EspaceResponse espace;
    private String erreur;

    // Champs modifiables du profil
    private String prenom;
    private String nom;
    private String telephone;

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

    private String espaceLogo;
    private String espaceCouverture;
    private String espaceRegistreCommerce;
    private String espaceNumeroNinea;
    private String espaceNumeroRccm;
    private String photosTexte;

    @PostConstruct
    public void charger() {
        try {
            profil = userApiClient.obtenir(session.getAccessToken(), session.getCompte().id());
            prenom = profil.firstName();
            nom = profil.lastName();
            telephone = profil.phone();
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        try {
            List<EspaceResponse> mesEspaces = espaceApiClient.mesEspaces(session.getAccessToken());
            // mesEspaces() ne renvoie pas l'adresse (uniquement GET /{id}) : on recharge le détail
            // complet du premier espace du compte pour disposer de la localisation actuelle.
            espace = mesEspaces.isEmpty() ? null : espaceApiClient.obtenir(mesEspaces.get(0).id());
            remplirChampsEspace();
        } catch (ApiException e) {
            // Pas encore de profil utilisateur exploitable côté espace-service : pas une erreur bloquante,
            // l'utilisateur voit simplement la proposition de créer son espace.
            espace = null;
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

    public String enregistrerProfil() {
        try {
            profil = userApiClient.mettreAJour(session.getAccessToken(), new UpdateUserRequest(prenom, nom, telephone));
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Profil mis à jour.", null));
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mise à jour impossible", e.getMessage()));
        }
        return null;
    }

    public String enregistrerEspace() {
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
                            espaceLogo,
                            espaceCouverture,
                            espaceRegistreCommerce,
                            espaceNumeroNinea,
                            espaceNumeroRccm,
                            urlsDepuisTexte(photosTexte)));
            remplirChampsEspace();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Espace mis à jour.", null));
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mise à jour impossible", e.getMessage()));
        }
        return null;
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

    public UserResponse getProfil() {
        return profil;
    }

    public EspaceResponse getEspace() {
        return espace;
    }

    public String getErreur() {
        return erreur;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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
}
