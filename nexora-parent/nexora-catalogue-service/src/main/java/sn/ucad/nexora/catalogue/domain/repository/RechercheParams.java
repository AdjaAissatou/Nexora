package sn.ucad.nexora.catalogue.domain.repository;

import java.math.BigDecimal;

/**
 * Objet valeur transportant tous les critères de recherche d'offres.
 * Utilisé par OffreRepository.search() et construit à partir du OffreSearchRequest.
 */
public class RechercheParams {

    /** Texte libre : cherché dans titre, description, catégorie, commune */
    private String q;

    /** Filtres */
    private String categorie;
    private Long idCategorie;
    private String typeEspace;
    private String commune;
    private String region;
    private Long idEspace;

    /**
     * Mode « gestion » : toutes les offres de l'espace (sauf supprimées), quel que soit leur statut ou
     * celui de l'espace — pour le professionnel lui-même, jamais pour la recherche publique.
     */
    private boolean gestion;
    /** Limite la recherche à ces offres (recherche par photo) ; null : pas de limite. */
    private java.util.List<Long> idsOffres;

    /** Filtre prix */
    private BigDecimal prixMin;
    private BigDecimal prixMax;

    /** Filtre type d'offre */
    private Boolean estProduit;     // true = produit, false = service, null = les deux
    private Boolean disponible;
    private Boolean avecPromotion;
    private Boolean espaceVerifie;
    private Boolean ouvertMaintenant;

    /** Autour d'un point (lieu public, position) : rayon en km ; distance calculée pour chaque offre. */
    private java.math.BigDecimal lat;
    private java.math.BigDecimal lng;
    private Double rayonKm;

    /** Tri : PERTINENCE, PRIX_ASC, PRIX_DESC, DATE_DESC, NOTE, DISTANCE */
    private String tri;

    /** Pagination */
    private int page;
    private int taille;

    public RechercheParams() {
        this.disponible = true;
        this.tri = "PERTINENCE";
        this.page = 0;
        this.taille = 20;
    }

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
    public boolean isGestion() { return gestion; }
    public void setGestion(boolean gestion) { this.gestion = gestion; }
    public Long getIdEspace() { return idEspace; }
    public void setIdEspace(Long idEspace) { this.idEspace = idEspace; }
    public BigDecimal getPrixMin() { return prixMin; }
    public void setPrixMin(BigDecimal prixMin) { this.prixMin = prixMin; }
    public BigDecimal getPrixMax() { return prixMax; }
    public void setPrixMax(BigDecimal prixMax) { this.prixMax = prixMax; }
    public Boolean getEstProduit() { return estProduit; }
    public void setEstProduit(Boolean estProduit) { this.estProduit = estProduit; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public Boolean getAvecPromotion() { return avecPromotion; }
    public void setAvecPromotion(Boolean avecPromotion) { this.avecPromotion = avecPromotion; }
    public Boolean getEspaceVerifie() { return espaceVerifie; }
    public void setEspaceVerifie(Boolean espaceVerifie) { this.espaceVerifie = espaceVerifie; }
    public String getTri() { return tri; }
    public void setTri(String tri) { this.tri = tri; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }
    public java.util.List<Long> getIdsOffres() { return idsOffres; }
    public void setIdsOffres(java.util.List<Long> idsOffres) { this.idsOffres = idsOffres; }
    public Boolean getOuvertMaintenant() { return ouvertMaintenant; }
    public void setOuvertMaintenant(Boolean ouvertMaintenant) { this.ouvertMaintenant = ouvertMaintenant; }
    public java.math.BigDecimal getLat() { return lat; }
    public void setLat(java.math.BigDecimal lat) { this.lat = lat; }
    public java.math.BigDecimal getLng() { return lng; }
    public void setLng(java.math.BigDecimal lng) { this.lng = lng; }
    public Double getRayonKm() { return rayonKm; }
    public void setRayonKm(Double rayonKm) { this.rayonKm = rayonKm; }
    public boolean isAutourDunPoint() { return lat != null && lng != null; }
}
