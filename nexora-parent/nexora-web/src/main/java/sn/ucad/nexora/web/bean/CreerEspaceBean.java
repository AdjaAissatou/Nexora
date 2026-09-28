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
import sn.ucad.nexora.web.dto.espace.CreateEspaceRequest;
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

    @PostConstruct
    public void charger() {
        try {
            types = espaceApiClient.listerTypes();
        } catch (ApiException e) {
            types = List.of();
        }
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
                            numeroRccm));
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
}
