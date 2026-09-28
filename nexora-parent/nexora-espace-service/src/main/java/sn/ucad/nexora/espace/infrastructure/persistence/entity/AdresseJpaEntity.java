package sn.ucad.nexora.espace.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "adresse")
public class AdresseJpaEntity {
    @Id
    @Column(name = "id_adresse")
    private Long id;

    @Column(name = "id_espace")
    private Long espaceId;

    private String pays;
    private String region;
    private String commune;
    private String quartier;

    @Column(name = "adresse_complete")
    private String adresseComplete;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean principale;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }
    public String getAdresseComplete() { return adresseComplete; }
    public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Boolean getPrincipale() { return principale; }
    public void setPrincipale(Boolean principale) { this.principale = principale; }
}
