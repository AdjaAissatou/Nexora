package sn.ucad.nexora.catalogue.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class UpdateOffreRequest {

    private Long idTypeOffre;
    private Long idCategorie;

    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean negociable;
    private boolean disponible = true;

    private String marque;
    private String modele;
    private String reference;
    private Integer quantiteStock;
    private String garantie;
    private Boolean neuf;

    private Integer dureeEstimee;
    private Boolean interventionDomicile;
    private Boolean reservation;

    private List<AttributValeurRequest> attributs;
    private List<String> images;

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
}
