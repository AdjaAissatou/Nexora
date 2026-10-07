package sn.ucad.nexora.catalogue.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class CreateOffreRequest {

    private Long idEspace;
    private Long idTypeOffre;
    private Long idCategorie;

    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean negociable;
    private boolean disponible = true;

    // Produit (si le type d'offre est PRODUIT)
    private String marque;
    private String modele;
    private String reference;
    private Integer quantiteStock;
    private String garantie;
    private Boolean neuf;

    // Service (si le type d'offre est SERVICE)
    private Integer dureeEstimee;
    private Boolean interventionDomicile;
    private Boolean reservation;

    private List<AttributValeurRequest> attributs;
    private List<String> images;

    public Long getIdEspace() { return idEspace; }
    public void setIdEspace(Long idEspace) { this.idEspace = idEspace; }
    public Long getIdTypeOffre() { return idTypeOffre; }
    public void setIdTypeOffre(Long idTypeOffre) { this.idTypeOffre = idTypeOffre; }
    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long idCategorie) { this.idCategorie = idCategorie; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean negociable) { this.negociable = negociable; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer quantiteStock) { this.quantiteStock = quantiteStock; }
    public String getGarantie() { return garantie; }
    public void setGarantie(String garantie) { this.garantie = garantie; }
    public Boolean getNeuf() { return neuf; }
    public void setNeuf(Boolean neuf) { this.neuf = neuf; }
    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
    public Boolean getInterventionDomicile() { return interventionDomicile; }
    public void setInterventionDomicile(Boolean interventionDomicile) { this.interventionDomicile = interventionDomicile; }
    public Boolean getReservation() { return reservation; }
    public void setReservation(Boolean reservation) { this.reservation = reservation; }
    public List<AttributValeurRequest> getAttributs() { return attributs; }
    public void setAttributs(List<AttributValeurRequest> attributs) { this.attributs = attributs; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    // « Autre… » (§21) : catégorie écrite par le professionnel et nature choisie (PRODUIT ou SERVICE).
    // Renseignée, elle remplace catégorie et type : l'offre va dans le rayon principal de l'espace.
    private String categorieProposee;
    private String natureProposee;

    public String getCategorieProposee() { return categorieProposee; }
    public void setCategorieProposee(String v) { this.categorieProposee = v; }
    public String getNatureProposee() { return natureProposee; }
    public void setNatureProposee(String v) { this.natureProposee = v; }
}
