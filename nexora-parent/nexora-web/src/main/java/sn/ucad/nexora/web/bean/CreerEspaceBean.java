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
import sn.ucad.nexora.web.dto.espace.CommuneResponse;
import sn.ucad.nexora.web.dto.espace.CreateEspaceRequest;
import sn.ucad.nexora.web.dto.espace.DepartementResponse;
import sn.ucad.nexora.web.dto.espace.RegionResponse;
import sn.ucad.nexora.web.dto.espace.TypeEspaceResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/** Backing bean de {@code creer-espace.xhtml}. */
@Named
@ViewScoped
public class CreerEspaceBean implements Serializable {

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private SessionBean session;

    private List<TypeEspaceResponse> types;

    private Long typeEspaceId;
    private String nom;
    private String slogan;
    private String description;
    private String telephone;
    private String telephoneSecondaire;
    private String email;
    private String siteWeb;
    private String registreCommerce;
    private String numeroNinea;
    private String numeroRccm;

    // Localisation — toujours choisie en cascade, jamais saisie librement pour région/département/commune.
    private List<RegionResponse> regions;
    private Long idRegion;
    private List<DepartementResponse> departements;
    private Long idDepartement;
    private List<CommuneResponse> communes;
    private Long idCommune;
    private String quartier;
    private String adresseComplete;

    @PostConstruct
    public void charger() {
        try {
            types = espaceApiClient.listerTypes();
        } catch (ApiException e) {
            types = List.of();
        }
        try {
            regions = espaceApiClient.regions();
        } catch (ApiException e) {
            regions = List.of();
        }
        if (session.getCompte() != null) {
            telephone = session.getCompte().phone();
            email = session.getCompte().email();
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

    public String creer() {
        try {
            espaceApiClient.creer(
                    session.getAccessToken(),
                    new CreateEspaceRequest(
                            typeEspaceId,
                            nom,
                            slogan,
                            description,
                            telephone,
                            telephoneSecondaire,
                            email,
                            siteWeb,
                            registreCommerce,
                            numeroNinea,
                            numeroRccm,
                            idRegion,
                            idDepartement,
                            idCommune,
                            quartier,
                            adresseComplete));
            return "mon-espace?faces-redirect=true";
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Création impossible", e.getMessage()));
            return null;
        }
    }

    public List<TypeEspaceResponse> getTypes() {
        return types;
    }

    public Long getTypeEspaceId() {
        return typeEspaceId;
    }

    public void setTypeEspaceId(Long typeEspaceId) {
        this.typeEspaceId = typeEspaceId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephoneSecondaire() {
        return telephoneSecondaire;
    }

    public void setTelephoneSecondaire(String telephoneSecondaire) {
        this.telephoneSecondaire = telephoneSecondaire;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getRegistreCommerce() {
        return registreCommerce;
    }

    public void setRegistreCommerce(String registreCommerce) {
        this.registreCommerce = registreCommerce;
    }

    public String getNumeroNinea() {
        return numeroNinea;
    }

    public void setNumeroNinea(String numeroNinea) {
        this.numeroNinea = numeroNinea;
    }

    public String getNumeroRccm() {
        return numeroRccm;
    }

    public void setNumeroRccm(String numeroRccm) {
        this.numeroRccm = numeroRccm;
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
}
