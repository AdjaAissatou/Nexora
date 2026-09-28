package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.EspaceApiClient;
import sn.ucad.nexora.web.dto.catalogue.AttributResponse;
import sn.ucad.nexora.web.dto.catalogue.AttributValeurRequest;
import sn.ucad.nexora.web.dto.catalogue.CategorieResponse;
import sn.ucad.nexora.web.dto.catalogue.CreateOffreRequest;
import sn.ucad.nexora.web.dto.catalogue.TypeOffreResponse;
import sn.ucad.nexora.web.dto.espace.EspaceResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Backing bean de {@code creer-offre.xhtml} : sélection en cascade catégorie → sous-catégorie
 * → type d'offre, puis attributs dynamiques (listes déroulantes propres à la catégorie) — pas de
 * saisie libre au-delà des champs génériques (titre, description, prix).
 */
@Named
@ViewScoped
public class CreerOffreBean implements Serializable {

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    @Inject
    private transient EspaceApiClient espaceApiClient;

    @Inject
    private SessionBean session;

    private EspaceResponse espace;
    private String erreur;

    private List<CategorieResponse> niveau1;
    private List<CategorieResponse> niveau2;
    private List<CategorieResponse> niveau3;
    private List<TypeOffreResponse> typesOffre;
    private List<AttributResponse> attributs;

    private Long niveau1Id;
    private Long niveau2Id;
    private Long niveau3Id;
    private Long typeOffreId;

    private final Map<Long, String> valeursListe = new HashMap<>();
    private final Map<Long, String> valeursTexte = new HashMap<>();
    private final Map<Long, BigDecimal> valeursNombre = new HashMap<>();

    // Champs communs
    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean negociable;

    // Produit
    private String marque;
    private String modele;
    private String reference;
    private Integer quantiteStock;
    private String garantie;
    private boolean neuf = true;

    // Service
    private Integer dureeEstimee;
    private boolean interventionDomicile;
    private boolean reservation = true;

    @PostConstruct
    public void charger() {
        try {
            List<EspaceResponse> mesEspaces = espaceApiClient.mesEspaces(session.getAccessToken());
            espace = mesEspaces.isEmpty() ? null : mesEspaces.get(0);
        } catch (ApiException e) {
            erreur = e.getMessage();
            return;
        }
        try {
            niveau1 = catalogueApiClient.categoriesRacines();
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    public void onNiveau1Change() {
        niveau2 = null;
        niveau3 = null;
        niveau2Id = null;
        niveau3Id = null;
        typesOffre = null;
        attributs = null;
        typeOffreId = null;
        if (niveau1Id == null) return;

        CategorieResponse c = trouver(niveau1, niveau1Id);
        if (c != null && c.aDesEnfants()) {
            niveau2 = catalogueApiClient.sousCategories(niveau1Id);
        } else {
            chargerFeuille(niveau1Id);
        }
    }

    public void onNiveau2Change() {
        niveau3 = null;
        niveau3Id = null;
        typesOffre = null;
        attributs = null;
        typeOffreId = null;
        if (niveau2Id == null) return;

        CategorieResponse c = trouver(niveau2, niveau2Id);
        if (c != null && c.aDesEnfants()) {
            niveau3 = catalogueApiClient.sousCategories(niveau2Id);
        } else {
            chargerFeuille(niveau2Id);
        }
    }

    public void onNiveau3Change() {
        typesOffre = null;
        attributs = null;
        typeOffreId = null;
        if (niveau3Id == null) return;
        chargerFeuille(niveau3Id);
    }

    public void onTypeOffreChange() {
        if (typeOffreId == null || typesOffre == null) return;
        typesOffre.stream()
                .filter(t -> t.id().equals(typeOffreId))
                .findFirst()
                .ifPresent(t -> {
                    if (titre == null || titre.isBlank()) {
                        titre = t.libelle();
                    }
                });
    }

    private void chargerFeuille(Long idCategorie) {
        typesOffre = catalogueApiClient.typesOffre(idCategorie);
        attributs = catalogueApiClient.attributs(idCategorie);
        valeursListe.clear();
        valeursTexte.clear();
        valeursNombre.clear();
    }

    private CategorieResponse trouver(List<CategorieResponse> liste, Long id) {
        if (liste == null) return null;
        return liste.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
    }

    /** Id de la catégorie feuille effectivement sélectionnée (le niveau le plus profond choisi). */
    public Long getCategorieFeuilleId() {
        if (niveau3Id != null) return niveau3Id;
        if (niveau2Id != null) return niveau2Id;
        return niveau1Id;
    }

    public boolean isTypeOffreService() {
        if (typeOffreId == null || typesOffre == null) return false;
        return typesOffre.stream()
                .filter(t -> t.id().equals(typeOffreId))
                .findFirst()
                .map(t -> "SERVICE".equals(t.principale()))
                .orElse(false);
    }

    public String publier() {
        try {
            List<AttributValeurRequest> valeurs = new ArrayList<>();
            if (attributs != null) {
                for (AttributResponse a : attributs) {
                    switch (a.typeChamp()) {
                        case "LISTE" -> {
                            String v = valeursListe.get(a.id());
                            if (v != null && !v.isBlank()) {
                                valeurs.add(new AttributValeurRequest(a.id(), null, null, null, Long.valueOf(v)));
                            }
                        }
                        case "NOMBRE" -> {
                            BigDecimal v = valeursNombre.get(a.id());
                            if (v != null) {
                                valeurs.add(new AttributValeurRequest(a.id(), null, v, null, null));
                            }
                        }
                        default -> {
                            String v = valeursTexte.get(a.id());
                            if (v != null && !v.isBlank()) {
                                valeurs.add(new AttributValeurRequest(a.id(), v, null, null, null));
                            }
                        }
                    }
                }
            }

            CreateOffreRequest requete = new CreateOffreRequest(
                    espace.id(),
                    typeOffreId,
                    getCategorieFeuilleId(),
                    titre,
                    description,
                    prix,
                    negociable,
                    true,
                    marque,
                    modele,
                    reference,
                    quantiteStock,
                    garantie,
                    neuf,
                    dureeEstimee,
                    interventionDomicile,
                    reservation,
                    valeurs);

            catalogueApiClient.creerOffre(session.getAccessToken(), requete);
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Offre publiée.", null));
            return "mon-espace?faces-redirect=true";
        } catch (ApiException e) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Publication impossible", e.getMessage()));
            return null;
        }
    }

    public EspaceResponse getEspace() { return espace; }
    public String getErreur() { return erreur; }

    public List<CategorieResponse> getNiveau1() { return niveau1; }
    public List<CategorieResponse> getNiveau2() { return niveau2; }
    public List<CategorieResponse> getNiveau3() { return niveau3; }
    public List<TypeOffreResponse> getTypesOffre() { return typesOffre; }
    public List<AttributResponse> getAttributs() { return attributs; }

    public Long getNiveau1Id() { return niveau1Id; }
    public void setNiveau1Id(Long v) { niveau1Id = v; }
    public Long getNiveau2Id() { return niveau2Id; }
    public void setNiveau2Id(Long v) { niveau2Id = v; }
    public Long getNiveau3Id() { return niveau3Id; }
    public void setNiveau3Id(Long v) { niveau3Id = v; }
    public Long getTypeOffreId() { return typeOffreId; }
    public void setTypeOffreId(Long v) { typeOffreId = v; }

    public Map<Long, String> getValeursListe() { return valeursListe; }
    public Map<Long, String> getValeursTexte() { return valeursTexte; }
    public Map<Long, BigDecimal> getValeursNombre() { return valeursNombre; }

    public String getTitre() { return titre; }
    public void setTitre(String v) { titre = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal v) { prix = v; }
    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean v) { negociable = v; }

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
    public boolean isNeuf() { return neuf; }
    public void setNeuf(boolean v) { neuf = v; }

    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer v) { dureeEstimee = v; }
    public boolean isInterventionDomicile() { return interventionDomicile; }
    public void setInterventionDomicile(boolean v) { interventionDomicile = v; }
    public boolean isReservation() { return reservation; }
    public void setReservation(boolean v) { reservation = v; }
}
