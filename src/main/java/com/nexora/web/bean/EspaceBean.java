package com.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bean de creation d'espace et de gestion des offres (produits / services)
 * dans "Mon espace". Les formulaires s'appuient sur {@link CatalogueBean}
 * pour des categories imbriquees et des attributs a listes deroulantes (EAV).
 */
@Named("espaceBean")
@ViewScoped
public class EspaceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private SessionBean session;
    @Inject private CatalogueBean catalogue;

    // --- Creation d'espace ---
    private String nomCommercial;
    private boolean produits;
    private boolean services;
    private String ville = "Dakar";
    private String telephone;

    // --- Ajout d'une offre (produit ou service) ---
    private boolean formOuvert;
    private String kind = "PRODUIT";                     // PRODUIT | SERVICE
    private Long categorieId;
    private String titre;
    private String prix;
    private Map<String, String> valeursAttributs = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        // Pre-coche selon la nature de l'espace existant (creation initiale).
        if (session.getEspace() != null) {
            this.produits = session.getEspace().isProduits();
            this.services = session.getEspace().isServices();
        }
    }

    // ------------------------------------------------------- Creation d'espace
    public String creer() {
        if (nomCommercial == null || nomCommercial.isBlank()) {
            avertir("Le nom commercial est obligatoire.");
            return null;
        }
        if (!produits && !services) {
            avertir("Choisissez au moins des produits ou des services.");
            return null;
        }
        session.creerEspace(nomCommercial.trim(), produits, services, ville, telephone);
        return "mon-espace?faces-redirect=true";
    }

    // ------------------------------------------------------------- Mon espace
    public void changerOnglet(String onglet) {
        if (session.getEspace() != null) session.getEspace().setOngletActif(onglet);
        annuler();
    }

    public void ouvrirForm(String kind) {
        this.kind = kind;
        this.formOuvert = true;
        this.categorieId = null;
        this.titre = null;
        this.prix = null;
        this.valeursAttributs = new LinkedHashMap<>();
    }

    public void annuler() {
        this.formOuvert = false;
        this.categorieId = null;
        this.titre = null;
        this.prix = null;
        this.valeursAttributs = new LinkedHashMap<>();
    }

    /** Recharge les attributs quand la categorie change (ajax). */
    public void onCategorieChange() {
        this.valeursAttributs = new LinkedHashMap<>();
    }

    public void enregistrer() {
        if (categorieId == null) { avertir("Choisissez une catégorie."); return; }
        if (titre == null || titre.isBlank()) { avertir("Le titre est obligatoire."); return; }

        StringBuilder resume = new StringBuilder();
        for (CatalogueBean.Attr a : catalogue.attributs(categorieId)) {
            String v = valeursAttributs.get(a.getNom());
            if (v != null && !v.isBlank()) {
                if (resume.length() > 0) resume.append(", ");
                resume.append(v);
            }
        }
        SessionBean.Offre offre = new SessionBean.Offre(
                titre.trim(), catalogue.nomDe(categorieId), prix, resume.toString());

        SessionBean.Espace e = session.getEspace();
        if (e != null) {
            if ("SERVICE".equals(kind)) e.getListeServices().add(offre);
            else e.getListeProduits().add(offre);
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Ajouté ! En attente de validation.", null));
        annuler();
    }

    public java.util.List<CatalogueBean.Attr> getAttributsCourants() {
        return catalogue.attributs(categorieId);
    }

    private void avertir(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    // --- Getters / Setters ---
    public String getNomCommercial()        { return nomCommercial; }
    public void setNomCommercial(String v)  { this.nomCommercial = v; }
    public boolean isProduits()             { return produits; }
    public void setProduits(boolean v)      { this.produits = v; }
    public boolean isServices()             { return services; }
    public void setServices(boolean v)      { this.services = v; }
    public String getVille()                { return ville; }
    public void setVille(String v)          { this.ville = v; }
    public String getTelephone()            { return telephone; }
    public void setTelephone(String v)      { this.telephone = v; }

    public boolean isFormOuvert()           { return formOuvert; }
    public String getKind()                 { return kind; }
    public void setKind(String v)           { this.kind = v; }
    public Long getCategorieId()            { return categorieId; }
    public void setCategorieId(Long v)      { this.categorieId = v; }
    public String getTitre()                { return titre; }
    public void setTitre(String v)          { this.titre = v; }
    public String getPrix()                 { return prix; }
    public void setPrix(String v)           { this.prix = v; }
    public Map<String, String> getValeursAttributs()       { return valeursAttributs; }
    public void setValeursAttributs(Map<String, String> v) { this.valeursAttributs = v; }
}
