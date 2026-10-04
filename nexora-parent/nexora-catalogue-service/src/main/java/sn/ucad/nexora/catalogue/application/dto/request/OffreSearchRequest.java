package sn.ucad.nexora.catalogue.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * Requête de recherche d'offres.
 * Tous les champs sont optionnels sauf la pagination.
 */
public class OffreSearchRequest {

    /** Texte libre (titre, description, catégorie, commune) */
    private String q;

    /** Filtres */
    private String categorie;
    private Long idCategorie;
    private String typeEspace;
    private String commune;
    private String region;
    private Long idEspace;
    private BigDecimal prixMin;
    private BigDecimal prixMax;

    /** null = tout, true = produits seulement, false = services seulement */
    private Boolean estProduit;
    private Boolean avecPromotion;
    private Boolean espaceVerifie;
    /** Uniquement les espaces ouverts en ce moment (horaires renseignés, heure de Dakar, §10). */
    private Boolean ouvertMaintenant;
    /** Autour d'un point : latitude, longitude, rayon en km. */
    private BigDecimal lat;
    private BigDecimal lng;
    private Double rayonKm;

    /** PERTINENCE | PRIX_ASC | PRIX_DESC | DATE_DESC | NOTE */
    private String tri = "PERTINENCE";

    @Min(0)
    private int page = 0;

    @Min(1) @Max(100)
    private int taille = 20;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public Long getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Long idCategorie) { this.idCategorie = idCategorie; }
    public String getTypeEspace() { return typeEspace; }
    public void setTypeEspace(String typeEspace) { this.typeEspace = typeEspace; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Long getIdEspace() { return idEspace; }
    public void setIdEspace(Long idEspace) { this.idEspace = idEspace; }
    public BigDecimal getPrixMin() { return prixMin; }
    public void setPrixMin(BigDecimal prixMin) { this.prixMin = prixMin; }
    public BigDecimal getPrixMax() { return prixMax; }
    public void setPrixMax(BigDecimal prixMax) { this.prixMax = prixMax; }
    public Boolean getEstProduit() { return estProduit; }
    public void setEstProduit(Boolean estProduit) { this.estProduit = estProduit; }
    public Boolean getAvecPromotion() { return avecPromotion; }
    public void setAvecPromotion(Boolean avecPromotion) { this.avecPromotion = avecPromotion; }
    public Boolean getOuvertMaintenant() { return ouvertMaintenant; }
    public void setOuvertMaintenant(Boolean ouvertMaintenant) { this.ouvertMaintenant = ouvertMaintenant; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public Double getRayonKm() { return rayonKm; }
    public void setRayonKm(Double rayonKm) { this.rayonKm = rayonKm; }
    public Boolean getEspaceVerifie() { return espaceVerifie; }
    public void setEspaceVerifie(Boolean espaceVerifie) { this.espaceVerifie = espaceVerifie; }
    public String getTri() { return tri; }
    public void setTri(String tri) { this.tri = tri; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }
}
