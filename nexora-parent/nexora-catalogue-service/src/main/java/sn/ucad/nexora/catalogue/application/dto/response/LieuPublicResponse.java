package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;

public class LieuPublicResponse {
    private Long id;
    private String nom;
    private String typeLieu;
    private String region;
    private String departement;
    private String commune;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getTypeLieu() { return typeLieu; }
    public void setTypeLieu(String typeLieu) { this.typeLieu = typeLieu; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
