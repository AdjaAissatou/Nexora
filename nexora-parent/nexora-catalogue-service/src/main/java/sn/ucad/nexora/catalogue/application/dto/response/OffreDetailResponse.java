package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fiche complète d'une offre : informations de l'offre, de l'espace,
 * la localisation, le produit/service associé, les images et les tags.
 */
public class OffreDetailResponse {

    // ============ OFFRE ============
    private Long id;
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
    private Long vueCount;
    private String statut;
    private LocalDateTime datePublication;

    // ============ CATEGORIE ============
    private Long categorieId;
    private String categorie;
    private String typeOffre;

    // ============ ESPACE ============
    private Long espaceId;
    private String espaceNom;
    private String espaceSlogan;
    private String espaceLogo;
    private String espaceCouverture;
    private String espaceTelephone;
    private String espaceEmail;
    private String espaceSiteWeb;
    private boolean espaceCertifie;
    private boolean espaceVerifie;
    private boolean espaceOuvert;
    private BigDecimal espaceNoteMoyenne;
    private Integer espaceNombreAvis;
    private Long espaceNombreVues;
    private Long espaceNombreFavoris;
    private String typeEspace;

    // ============ LOCALISATION ============
    private LocalisationResponse localisation;

    // ============ PROMOTION ============
    private PromotionResponse promotion;

    // ============ IMAGES ============
    private List<String> images;
    private String imagePrincipale;

    // ============ PRODUIT (si applicable) ============
    private ProduitResponse produit;

    // ============ SERVICE (si applicable) ============
    private ServiceResponse service;

    // ============ TAGS ============
    private List<String> tags;

    // ============ GETTERS / SETTERS ============

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getTypeOffre() { return typeOffre; }
    public void setTypeOffre(String typeOffre) { this.typeOffre = typeOffre; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public String getEspaceNom() { return espaceNom; }
    public void setEspaceNom(String espaceNom) { this.espaceNom = espaceNom; }
    public String getEspaceSlogan() { return espaceSlogan; }
    public void setEspaceSlogan(String espaceSlogan) { this.espaceSlogan = espaceSlogan; }
    public String getEspaceLogo() { return espaceLogo; }
    public void setEspaceLogo(String espaceLogo) { this.espaceLogo = espaceLogo; }
    public String getEspaceCouverture() { return espaceCouverture; }
    public void setEspaceCouverture(String espaceCouverture) { this.espaceCouverture = espaceCouverture; }
    public String getEspaceTelephone() { return espaceTelephone; }
    public void setEspaceTelephone(String espaceTelephone) { this.espaceTelephone = espaceTelephone; }
    public String getEspaceEmail() { return espaceEmail; }
    public void setEspaceEmail(String espaceEmail) { this.espaceEmail = espaceEmail; }
    public String getEspaceSiteWeb() { return espaceSiteWeb; }
    public void setEspaceSiteWeb(String espaceSiteWeb) { this.espaceSiteWeb = espaceSiteWeb; }
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
    public Long getEspaceNombreVues() { return espaceNombreVues; }
    public void setEspaceNombreVues(Long espaceNombreVues) { this.espaceNombreVues = espaceNombreVues; }
    public Long getEspaceNombreFavoris() { return espaceNombreFavoris; }
    public void setEspaceNombreFavoris(Long espaceNombreFavoris) { this.espaceNombreFavoris = espaceNombreFavoris; }
    public String getTypeEspace() { return typeEspace; }
    public void setTypeEspace(String typeEspace) { this.typeEspace = typeEspace; }
    public LocalisationResponse getLocalisation() { return localisation; }
    public void setLocalisation(LocalisationResponse localisation) { this.localisation = localisation; }
    public PromotionResponse getPromotion() { return promotion; }
    public void setPromotion(PromotionResponse promotion) { this.promotion = promotion; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getImagePrincipale() { return imagePrincipale; }
    public void setImagePrincipale(String imagePrincipale) { this.imagePrincipale = imagePrincipale; }
    public ProduitResponse getProduit() { return produit; }
    public void setProduit(ProduitResponse produit) { this.produit = produit; }
    public ServiceResponse getService() { return service; }
    public void setService(ServiceResponse service) { this.service = service; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // ============ CLASSES IMBRIQUÉES ============

    public static class LocalisationResponse {
        private String pays;
        private String region;
        private String departement;
        private String commune;
        private String arrondissement;
        private String quartier;
        private String adresseComplete;
        private String codePostal;
        private BigDecimal latitude;
        private BigDecimal longitude;

        public String getPays() { return pays; }
        public void setPays(String pays) { this.pays = pays; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getDepartement() { return departement; }
        public void setDepartement(String departement) { this.departement = departement; }
        public String getCommune() { return commune; }
        public void setCommune(String commune) { this.commune = commune; }
        public String getArrondissement() { return arrondissement; }
        public void setArrondissement(String arrondissement) { this.arrondissement = arrondissement; }
        public String getQuartier() { return quartier; }
        public void setQuartier(String quartier) { this.quartier = quartier; }
        public String getAdresseComplete() { return adresseComplete; }
        public void setAdresseComplete(String adresseComplete) { this.adresseComplete = adresseComplete; }
        public String getCodePostal() { return codePostal; }
        public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    }

    public static class PromotionResponse {
        private String nom;
        private String typeReduction;
        private BigDecimal valeur;
        private LocalDateTime dateFin;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getTypeReduction() { return typeReduction; }
        public void setTypeReduction(String typeReduction) { this.typeReduction = typeReduction; }
        public BigDecimal getValeur() { return valeur; }
        public void setValeur(BigDecimal valeur) { this.valeur = valeur; }
        public LocalDateTime getDateFin() { return dateFin; }
        public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    }

    public static class ProduitResponse {
        private String marque;
        private String modele;
        private String reference;
        private Integer quantiteStock;
        private Double poids;
        private String garantie;
        private boolean neuf;

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

    public static class ServiceResponse {
        private Integer dureeEstimee;
        private boolean interventionDomicile;
        private boolean interventionDistance;
        private Integer delaiReponse;
        private boolean reservation;
        private boolean urgence;

        public Integer getDureeEstimee() { return dureeEstimee; }
        public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
        public boolean isInterventionDomicile() { return interventionDomicile; }
        public void setInterventionDomicile(boolean interventionDomicile) { this.interventionDomicile = interventionDomicile; }
        public boolean isInterventionDistance() { return interventionDistance; }
        public void setInterventionDistance(boolean interventionDistance) { this.interventionDistance = interventionDistance; }
        public Integer getDelaiReponse() { return delaiReponse; }
        public void setDelaiReponse(Integer delaiReponse) { this.delaiReponse = delaiReponse; }
        public boolean isReservation() { return reservation; }
        public void setReservation(boolean reservation) { this.reservation = reservation; }
        public boolean isUrgence() { return urgence; }
        public void setUrgence(boolean urgence) { this.urgence = urgence; }
    }
}
