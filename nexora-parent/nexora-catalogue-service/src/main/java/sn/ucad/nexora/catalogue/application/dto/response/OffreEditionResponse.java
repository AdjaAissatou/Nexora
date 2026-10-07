package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Détail d'une offre destiné à préremplir le formulaire d'édition (pas la fiche publique) :
 * la chaîne de catégories (racine > sous-catégorie > feuille) pour la cascade, et les valeurs
 * d'attributs déjà saisies. Accessible uniquement au propriétaire de l'espace.
 */
public class OffreEditionResponse {
    private Long id;
    private Long idEspace;
    private Long niveau1Id;
    private Long niveau2Id;
    private Long niveau3Id;
    private Long idCategorie;
    private Long idTypeOffre;
    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean negociable;
    private boolean disponible;
    private String marque;
    private String modele;
    private String reference;
    private Integer quantiteStock;
    private String garantie;
    private Boolean neuf;
    private Integer dureeEstimee;
    private Boolean interventionDomicile;
    private Boolean reservation;
    private List<AttributValeurResponse> attributs;
    private List<String> images;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Long getIdEspace() { return idEspace; }
    public void setIdEspace(Long v) { idEspace = v; }
    public Long getNiveau1Id() { return niveau1Id; }
    public void setNiveau1Id(Long v) { niveau1Id = v; }
    public Long getNiveau2Id() { return niveau2Id; }
    public void setNiveau2Id(Long v) { niveau2Id = v; }
    public Long getNiveau3Id() { return niveau3Id; }
    public void setNiveau3Id(Long v) { niveau3Id = v; }
    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long v) { idCategorie = v; }
    public Long getIdTypeOffre() { return idTypeOffre; }
    public void setIdTypeOffre(Long v) { idTypeOffre = v; }
    public String getTitre() { return titre; }
    public void setTitre(String v) { titre = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal v) { prix = v; }
    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean v) { negociable = v; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean v) { disponible = v; }
    public String getMarque() { return marque; }
    public void setMarque(String v) { marque = v; }
    public String getModele() { return modele; }
    public void setModele(String v) { modele = v; }
    public String getReference() { return reference; }
    public void setReference(String v) { reference = v; }
    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer v) { quantiteStock = v; }
    public String getGarantie() { return garantie; }
    public void setGarantie(String v) { garantie = v; }
    public Boolean getNeuf() { return neuf; }
    public void setNeuf(Boolean v) { neuf = v; }
    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer v) { dureeEstimee = v; }
    public Boolean getInterventionDomicile() { return interventionDomicile; }
    public void setInterventionDomicile(Boolean v) { interventionDomicile = v; }
    public Boolean getReservation() { return reservation; }
    public void setReservation(Boolean v) { reservation = v; }
    public List<AttributValeurResponse> getAttributs() { return attributs; }
    public void setAttributs(List<AttributValeurResponse> v) { attributs = v; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> v) { images = v; }

    /** « Autre… » (§21) : catégorie écrite par le professionnel, encore en attente ; nature de l'offre. */
    private String categorieProposee;
    private String nature;

    public String getCategorieProposee() { return categorieProposee; }
    public void setCategorieProposee(String v) { categorieProposee = v; }
    public String getNature() { return nature; }
    public void setNature(String v) { nature = v; }
}
