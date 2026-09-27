package sn.ucad.nexora.recherche.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class SauvegarderRechercheRequest {
    @NotBlank
    private String nom;
    private String motCle;
    private String categorie;
    private Integer rayonKm;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getMotCle() { return motCle; }
    public void setMotCle(String motCle) { this.motCle = motCle; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public Integer getRayonKm() { return rayonKm; }
    public void setRayonKm(Integer rayonKm) { this.rayonKm = rayonKm; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}
