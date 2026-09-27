package sn.ucad.nexora.recherche.application.dto.request;

import java.math.BigDecimal;

/** Enregistrement automatique d'une recherche effectuée. */
public class EnregistrerRechercheRequest {
    private String motCle;
    private String categorie;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer rayonKm;
    private Integer nombreResultats;

    public String getMotCle() { return motCle; }
    public void setMotCle(String motCle) { this.motCle = motCle; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getRayonKm() { return rayonKm; }
    public void setRayonKm(Integer rayonKm) { this.rayonKm = rayonKm; }
    public Integer getNombreResultats() { return nombreResultats; }
    public void setNombreResultats(Integer nombreResultats) { this.nombreResultats = nombreResultats; }
}
