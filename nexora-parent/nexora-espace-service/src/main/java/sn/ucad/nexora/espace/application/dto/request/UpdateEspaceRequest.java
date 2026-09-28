package sn.ucad.nexora.espace.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdateEspaceRequest {
    @NotBlank private String nom;
    private String slogan;
    private String description;
    private String telephone;
    private String telephoneSecondaire;
    private String email;
    private String siteWeb;
    private Boolean ouvert;
    @NotNull private Long idRegion;
    @NotNull private Long idDepartement;
    @NotNull private Long idCommune;
    @NotBlank private String quartier;
    private String adresseComplete;
    private String logo;
    private String couverture;
    private String registreCommerce;
    private String numeroNinea;
    private String numeroRccm;
    private List<String> photos;

    public String getNom() { return nom; }
    public void setNom(String v) { nom = v; }
    public String getSlogan() { return slogan; }
    public void setSlogan(String v) { slogan = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String v) { telephone = v; }
    public String getTelephoneSecondaire() { return telephoneSecondaire; }
    public void setTelephoneSecondaire(String v) { telephoneSecondaire = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String v) { siteWeb = v; }
    public Boolean getOuvert() { return ouvert; }
    public void setOuvert(Boolean v) { ouvert = v; }
    public Long getIdRegion() { return idRegion; }
    public void setIdRegion(Long v) { idRegion = v; }
    public Long getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Long v) { idDepartement = v; }
    public Long getIdCommune() { return idCommune; }
    public void setIdCommune(Long v) { idCommune = v; }
    public String getQuartier() { return quartier; }
    public void setQuartier(String v) { quartier = v; }
    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String v) { adresseComplete = v; }
    public String getLogo() { return logo; }
    public void setLogo(String v) { logo = v; }
    public String getCouverture() { return couverture; }
    public void setCouverture(String v) { couverture = v; }
    public String getRegistreCommerce() { return registreCommerce; }
    public void setRegistreCommerce(String v) { registreCommerce = v; }
    public String getNumeroNinea() { return numeroNinea; }
    public void setNumeroNinea(String v) { numeroNinea = v; }
    public String getNumeroRccm() { return numeroRccm; }
    public void setNumeroRccm(String v) { numeroRccm = v; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> v) { photos = v; }
}
