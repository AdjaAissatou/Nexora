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
    private String typeEspace;
    private String commune;
    private String region;
    private Long idEspace;

    /** Filtre prix */
    private BigDecimal prixMin;
    private BigDecimal prixMax;

    /** Filtre type d'offre */
    private Boolean estProduit;     // true = produit, false = service, null = les deux
    private Boolean disponible;
    private Boolean avecPromotion;
    private Boolean espaceVerifie;

    /** Tri : PERTINENCE, PRIX_ASC, PRIX_DESC, DATE_DESC, NOTE */
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
}
