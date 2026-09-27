package sn.ucad.nexora.catalogue.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité JPA mappant la table `offre` (lecture/écriture).
 * Les requêtes de recherche utilisent une requête JPQL/native enrichie
 * via OffreSearchJpaRepository (vue vue_recherche_globale).
 */
@Entity
@Table(name = "offre")
public class OffreJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long id;

    @Column(name = "id_espace", nullable = false)
    private Long espaceId;

    @Column(name = "id_type_offre", nullable = false)
    private Long typeOffreId;

    @Column(name = "id_categorie", nullable = false)
    private Long categorieId;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal prix;

    @Column(name = "ancien_prix", precision = 12, scale = 2)
    private BigDecimal ancienPrix;

    @Column(name = "negociable")
    private Boolean negociable = false;

    @Column(name = "disponible")
    private Boolean disponible = true;

    @Column(name = "est_commandable")
    private Boolean estCommandable = true;

    @Column(name = "est_reservable")
    private Boolean estReservable = false;

    @Column(name = "stockable")
    private Boolean stockable = false;

    @Column(name = "quantite_disponible")
    private Integer quantiteDisponible;

    @Column(name = "necessite_validation")
    private Boolean necessite_validation = false;

    @Column(name = "vue_count")
    private Long vueCount = 0L;

    @Column(name = "score_pertinence")
    private Double scorePertinence = 0.0;

    @Column(name = "statut", columnDefinition = "statut_offre")
    private String statut = "BROUILLON";

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    // ============ GETTERS / SETTERS ============

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public Long getTypeOffreId() { return typeOffreId; }
    public void setTypeOffreId(Long typeOffreId) { this.typeOffreId = typeOffreId; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public BigDecimal getAncienPrix() { return ancienPrix; }
    public void setAncienPrix(BigDecimal ancienPrix) { this.ancienPrix = ancienPrix; }
    public Boolean getNegociable() { return negociable; }
    public void setNegociable(Boolean negociable) { this.negociable = negociable; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public Boolean getEstCommandable() { return estCommandable; }
    public void setEstCommandable(Boolean estCommandable) { this.estCommandable = estCommandable; }
    public Boolean getEstReservable() { return estReservable; }
    public void setEstReservable(Boolean estReservable) { this.estReservable = estReservable; }
    public Boolean getStockable() { return stockable; }
    public void setStockable(Boolean stockable) { this.stockable = stockable; }
    public Integer getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(Integer quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }
    public Long getVueCount() { return vueCount; }
    public void setVueCount(Long vueCount) { this.vueCount = vueCount; }
    public Double getScorePertinence() { return scorePertinence; }
    public void setScorePertinence(Double scorePertinence) { this.scorePertinence = scorePertinence; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }
}
