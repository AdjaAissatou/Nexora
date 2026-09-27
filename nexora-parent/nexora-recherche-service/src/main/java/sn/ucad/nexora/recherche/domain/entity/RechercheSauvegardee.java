package sn.ucad.nexora.recherche.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Alerte / recherche enregistrée par l'utilisateur pour être réutilisée.
 */
public class RechercheSauvegardee {

    private Long id;
    private Long utilisateurId;
    private String nom;
    private String motCle;
    private String categorie;
    private Integer rayonKm;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean active;
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
