package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;

/**
 * Réponse légère retournée dans les listes de recherche.
 * N'inclut pas les détails complets produit/service/attributs.
 */
public class OffreSummaryResponse {

    private Long id;
    private String titre;
    private BigDecimal prix;
    private BigDecimal ancienPrix;
    private String imagePrincipale;
    private boolean disponible;
    private boolean negociable;

    // Statut et modération : utiles à la liste de gestion du professionnel (PUBLIE en recherche publique)
    private String statut;
    private String motifModeration;
    /** Ouvert en ce moment : null si l'espace n'a pas renseigné ses horaires (§10). */
    private Boolean espaceOuvertMaintenant;
    /** Distance au point de recherche (km), null hors recherche autour d'un point. */
    private Double distanceKm;

    // Espace
    private Long espaceId;
    private String espaceNom;
    private String espaceLogo;
    private boolean espaceCertifie;
    private boolean espaceVerifie;
    private BigDecimal espaceNoteMoyenne;
    private Integer espaceNombreAvis;
    private String typeEspace;

    // Catégorie
    private String categorie;

    // Localisation (résumée)
    private String commune;
    private String region;
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Promotion active
    private String promotionNom;
    private String typeReduction;
    private BigDecimal valeurReduction;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public BigDecimal getAncienPrix() { return ancienPrix; }
    public void setAncienPrix(BigDecimal ancienPrix) { this.ancienPrix = ancienPrix; }
    public String getImagePrincipale() { return imagePrincipale; }
    public void setImagePrincipale(String imagePrincipale) { this.imagePrincipale = imagePrincipale; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean negociable) { this.negociable = negociable; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public String getEspaceNom() { return espaceNom; }
    public void setEspaceNom(String espaceNom) { this.espaceNom = espaceNom; }
    public String getEspaceLogo() { return espaceLogo; }
    public void setEspaceLogo(String espaceLogo) { this.espaceLogo = espaceLogo; }
    public boolean isEspaceCertifie() { return espaceCertifie; }
    public void setEspaceCertifie(boolean espaceCertifie) { this.espaceCertifie = espaceCertifie; }
    public boolean isEspaceVerifie() { return espaceVerifie; }
    public void setEspaceVerifie(boolean espaceVerifie) { this.espaceVerifie = espaceVerifie; }
    public BigDecimal getEspaceNoteMoyenne() { return espaceNoteMoyenne; }
    public void setEspaceNoteMoyenne(BigDecimal espaceNoteMoyenne) { this.espaceNoteMoyenne = espaceNoteMoyenne; }
    public Integer getEspaceNombreAvis() { return espaceNombreAvis; }
    public void setEspaceNombreAvis(Integer espaceNombreAvis) { this.espaceNombreAvis = espaceNombreAvis; }
    public String getTypeEspace() { return typeEspace; }
    public void setTypeEspace(String typeEspace) { this.typeEspace = typeEspace; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
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

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Boolean getEspaceOuvertMaintenant() { return espaceOuvertMaintenant; }
    public void setEspaceOuvertMaintenant(Boolean v) { this.espaceOuvertMaintenant = v; }
    public String getMotifModeration() { return motifModeration; }
    public void setMotifModeration(String motifModeration) { this.motifModeration = motifModeration; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
