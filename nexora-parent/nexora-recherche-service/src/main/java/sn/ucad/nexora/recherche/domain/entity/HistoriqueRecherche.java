package sn.ucad.nexora.recherche.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représente une recherche effectuée par un utilisateur.
 * Enregistrée automatiquement à chaque appel de recherche.
 */
public class HistoriqueRecherche {

    private Long id;
    private Long utilisateurId;   // nullable (recherche anonyme possible)
    private String motCle;
    private String categorie;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer rayonKm;
    private Integer nombreResultats;
    private LocalDateTime dateRecherche;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
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
    public LocalDateTime getDateRecherche() { return dateRecherche; }
    public void setDateRecherche(LocalDateTime dateRecherche) { this.dateRecherche = dateRecherche; }
}
