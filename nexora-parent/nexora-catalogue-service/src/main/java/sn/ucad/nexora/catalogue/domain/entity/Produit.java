package sn.ucad.nexora.catalogue.domain.entity;

public class Produit {

    private Long id;
    private Long offreId;
    private String marque;
    private String modele;
    private String reference;
    private Integer quantiteStock;
    private Double poids;
    private String garantie;
    private boolean neuf;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer quantiteStock) { this.quantiteStock = quantiteStock; }
    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }
    public String getGarantie() { return garantie; }
    public void setGarantie(String garantie) { this.garantie = garantie; }
    public boolean isNeuf() { return neuf; }
    public void setNeuf(boolean neuf) { this.neuf = neuf; }
}
