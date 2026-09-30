package sn.ucad.nexora.catalogue.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité domaine représentant une offre (produit ou service) dans NEXORA.
 * Contient les informations de l'offre enrichies avec la localisation et l'espace.
 */
public class Offre {

    private Long id;
    private Long espaceId;
    private Long typeOffreId;
    private Long categorieId;

    private String titre;
    private String description;
    private BigDecimal prix;
    private BigDecimal ancienPrix;
    private boolean negociable;
    private boolean disponible;
    private boolean estCommandable;
    private boolean estReservable;
    private boolean stockable;
    private Integer quantiteDisponible;
    private boolean necessite_validation;
    private Long vueCount;
    private Double scorePertinence;
    private String statut;
    private String motifModeration;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime datePublication;

    // Informations enrichies de l'espace
    private String espaceNom;
    private String espaceLogo;
    private String espaceTelephone;
    private boolean espaceCertifie;
    private boolean espaceVerifie;
    private boolean espaceOuvert;
    private BigDecimal espaceNoteMoyenne;
    private Integer espaceNombreAvis;
    private String typeEspace;
    private String categorie;

    // Localisation
    private String pays;
    private String region;
    private String departement;
    private String commune;
    private String quartier;
    private String adresseComplete;
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Promotion active
    private String promotionNom;
    private String typeReduction;
    private BigDecimal valeurReduction;

    // Images
    private List<String> images;
    private String imagePrincipale;

    // Attributs spécifiques (produit ou service)
    private Produit produit;
    private Service service;

    // Tags
    private List<String> tags;

    // ===================== GETTERS / SETTERS =====================

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

    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean negociable) { this.negociable = negociable; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public boolean isEstCommandable() { return estCommandable; }
    public void setEstCommandable(boolean estCommandable) { this.estCommandable = estCommandable; }

    public boolean isEstReservable() { return estReservable; }
    public void setEstReservable(boolean estReservable) { this.estReservable = estReservable; }

    public boolean isStockable() { return stockable; }
    public void setStockable(boolean stockable) { this.stockable = stockable; }

    public Integer getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(Integer quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }

    public Long getVueCount() { return vueCount; }
    public void setVueCount(Long vueCount) { this.vueCount = vueCount; }

    public Double getScorePertinence() { return scorePertinence; }
    public void setScorePertinence(Double scorePertinence) { this.scorePertinence = scorePertinence; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMotifModeration() { return motifModeration; }
    public void setMotifModeration(String motifModeration) { this.motifModeration = motifModeration; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public String getEspaceNom() { return espaceNom; }
    public void setEspaceNom(String espaceNom) { this.espaceNom = espaceNom; }

    public String getEspaceLogo() { return espaceLogo; }
    public void setEspaceLogo(String espaceLogo) { this.espaceLogo = espaceLogo; }

    public String getEspaceTelephone() { return espaceTelephone; }
    public void setEspaceTelephone(String espaceTelephone) { this.espaceTelephone = espaceTelephone; }

    public boolean isEspaceCertifie() { return espaceCertifie; }
    public void setEspaceCertifie(boolean espaceCertifie) { this.espaceCertifie = espaceCertifie; }

    public boolean isEspaceVerifie() { return espaceVerifie; }
    public void setEspaceVerifie(boolean espaceVerifie) { this.espaceVerifie = espaceVerifie; }

    public boolean isEspaceOuvert() { return espaceOuvert; }
    public void setEspaceOuvert(boolean espaceOuvert) { this.espaceOuvert = espaceOuvert; }

    public BigDecimal getEspaceNoteMoyenne() { return espaceNoteMoyenne; }
    public void setEspaceNoteMoyenne(BigDecimal espaceNoteMoyenne) { this.espaceNoteMoyenne = espaceNoteMoyenne; }

    public Integer getEspaceNombreAvis() { return espaceNombreAvis; }
    public void setEspaceNombreAvis(Integer espaceNombreAvis) { this.espaceNombreAvis = espaceNombreAvis; }

    public String getTypeEspace() { return typeEspace; }
    public void setTypeEspace(String typeEspace) { this.typeEspace = typeEspace; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

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

    public String getPromotionNom() { return promotionNom; }
    public void setPromotionNom(String promotionNom) { this.promotionNom = promotionNom; }

    public String getTypeReduction() { return typeReduction; }
    public void setTypeReduction(String typeReduction) { this.typeReduction = typeReduction; }

    public BigDecimal getValeurReduction() { return valeurReduction; }
    public void setValeurReduction(BigDecimal valeurReduction) { this.valeurReduction = valeurReduction; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getImagePrincipale() { return imagePrincipale; }
    public void setImagePrincipale(String imagePrincipale) { this.imagePrincipale = imagePrincipale; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
