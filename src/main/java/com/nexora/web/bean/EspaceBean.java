package com.nexora.web.bean;

import com.nexora.dto.EspaceRequest;
import com.nexora.dto.EspaceViewDTO;
import com.nexora.dto.OffreDTO;
import com.nexora.dto.OffreRequest;
import com.nexora.dto.ref.AttributDTO;
import com.nexora.service.CategorieService;
import com.nexora.service.EspaceService;
import com.nexora.service.OffreService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creation d'espace et gestion des offres (produits / services) dans
 * "Mon espace". Persistance reelle : {@link EspaceService} et
 * {@link OffreService}. Les categories/attributs viennent de
 * {@link CategorieService}. Aucune donnee simulee.
 */
@Named("espaceBean")
@ViewScoped
public class EspaceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private SessionBean session;
    @Inject private EspaceService espaceService;
    @Inject private OffreService offreService;
    @Inject private CategorieService categorieService;

    // --- Creation d'espace ---
    private String nomCommercial;
    private String nature = "MIXTE";       // BOUTIQUE | PRESTATAIRE | MIXTE
    private String ville = "Dakar";
    private String telephone;

    // --- Edition (correction d'une annonce rejetee) ---
    private Long editionId;

    // --- Onglet + formulaire d'ajout ---
    private String onglet = "produits";         // produits | services | stats
    private boolean formOuvert;
    private String kind = "PRODUIT";            // PRODUIT | SERVICE
    private Long categorieId;
    private String titre;
    private String prix;
    private Map<Long, String> valeursAttributs = new LinkedHashMap<>();

    // --- Caches (charges depuis la base) ---
    private List<OffreDTO> produits;
    private List<OffreDTO> services;

    @PostConstruct
    public void init() {
        // Onglet par defaut adapte a la nature : un prestataire demarre sur Services.
        if (session.getEspaceId() != null && !session.isAffProduits()) this.onglet = "services";
    }

    // ------------------------------------------------------- Creation d'espace
    public String creer() {
        if (!session.isConnecte()) return "connexion?faces-redirect=true";
        if (nomCommercial == null || nomCommercial.isBlank()) {
            avertir("Le nom commercial est obligatoire.");
            return null;
        }
        EspaceRequest req = new EspaceRequest();
        req.setNomCommercial(nomCommercial.trim());
        req.setNature(nature);
        req.setTelephonePrincipal(telephone);
        req.setPays("Sénégal");
        req.setRegion("Dakar");
        req.setVille(ville);
        try {
            EspaceViewDTO vue = espaceService.creer(session.getIdUtilisateur(), req);
            session.setEspace(vue, ville);
        } catch (Exception ex) {
            avertir("Création impossible : " + racine(ex));
            return null;
        }
        return "mon-espace?faces-redirect=true";
    }

    // ------------------------------------------------------------- Mon espace
    private void recharger() {
        produits = new ArrayList<>();
        services = new ArrayList<>();
        if (session.getEspaceId() == null) return;
        for (OffreDTO o : offreService.offresDeEspace(session.getEspaceId())) {
            if ("SERVICE".equals(o.getType())) services.add(o);
            else produits.add(o);
        }
    }

    public List<OffreDTO> getProduits() { if (produits == null) recharger(); return produits; }
    public List<OffreDTO> getServices() { if (services == null) recharger(); return services; }

    // --- Compteurs d'apercu (tableau de bord de l'espace) ---
    public int getNbProduits()  { return getProduits().size(); }
    public int getNbServices()  { return getServices().size(); }
    public long getNbTotal()    { return getNbProduits() + getNbServices(); }
    public long getNbEnLigne()  { return parStatut("PUBLIEE"); }
    public long getNbEnAttente(){ return parStatut("EN_ATTENTE_VALIDATION"); }
    public long getNbRejete()   { return parStatut("REJETEE"); }
    private long parStatut(String s) {
        long c = 0;
        for (OffreDTO o : getProduits()) if (s.equals(o.getStatut())) c++;
        for (OffreDTO o : getServices()) if (s.equals(o.getStatut())) c++;
        return c;
    }

    public String getOnglet() { return onglet; }
    public void changerOnglet(String o) { this.onglet = o; annuler(); }

    public void ouvrirForm(String kind) {
        this.kind = kind;
        this.editionId = null;
        this.formOuvert = true;
        this.categorieId = null;
        this.titre = null;
        this.prix = null;
        this.valeursAttributs = new LinkedHashMap<>();
    }

    /** Ouvre le formulaire pré-rempli pour corriger une annonce (ex. rejetée). */
    public void ouvrirEdition(OffreDTO o) {
        this.editionId = o.getId();
        this.kind = "SERVICE".equals(o.getType()) ? "SERVICE" : "PRODUIT";
        this.formOuvert = true;
        this.categorieId = o.getIdCategorie();
        this.titre = o.getTitre();
        this.prix = o.getPrix() != null ? o.getPrix().toPlainString() : null;
        this.valeursAttributs = new LinkedHashMap<>();
    }

    public void annuler() {
        this.formOuvert = false;
        this.editionId = null;
        this.categorieId = null;
        this.titre = null;
        this.prix = null;
        this.valeursAttributs = new LinkedHashMap<>();
    }

    public void onCategorieChange() { this.valeursAttributs = new LinkedHashMap<>(); }

    public List<AttributDTO> getAttributsCourants() {
        return categorieId == null ? List.of() : categorieService.attributsDeCategorie(categorieId);
    }

    public void enregistrer() {
        if (categorieId == null) { avertir("Choisissez une catégorie."); return; }
        if (titre == null || titre.isBlank()) { avertir("Le titre est obligatoire."); return; }

        OffreRequest req = new OffreRequest();
        req.setType(kind);
        req.setTitre(titre.trim());
        req.setPrix(parsePrix(prix));
        req.setIdEspace(session.getEspaceId());
        req.setIdCategorie(categorieId);
        Map<Long, String> attrs = new LinkedHashMap<>();
        valeursAttributs.forEach((k, v) -> { if (v != null && !v.isBlank()) attrs.put(k, v); });
        req.setAttributsTexte(attrs);
        boolean edition = editionId != null;
        try {
            if (edition) offreService.modifier(editionId, req);
            else offreService.publier(req);
        } catch (Exception ex) {
            avertir((edition ? "Correction" : "Publication") + " impossible : " + racine(ex));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                edition ? "Annonce corrigée et renvoyée en validation." : "Publié ! En attente de validation.", null));
        annuler();
        recharger();
    }

    private static BigDecimal parsePrix(String p) {
        if (p == null || p.isBlank()) return null;
        try { return new BigDecimal(p.replaceAll("[^0-9.]", "")); } catch (Exception e) { return null; }
    }

    private static String racine(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    private void avertir(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    // --- Getters / Setters ---
    public String getNomCommercial()        { return nomCommercial; }
    public void setNomCommercial(String v)  { this.nomCommercial = v; }
    public String getNature()               { return nature; }
    public void setNature(String v)         { this.nature = v; }
    public boolean isEdition()              { return editionId != null; }
    public String getVille()                { return ville; }
    public void setVille(String v)          { this.ville = v; }
    public String getTelephone()            { return telephone; }
    public void setTelephone(String v)      { this.telephone = v; }

    public boolean isFormOuvert()           { return formOuvert; }
    public String getKind()                 { return kind; }
    public Long getCategorieId()            { return categorieId; }
    public void setCategorieId(Long v)      { this.categorieId = v; }
    public String getTitre()                { return titre; }
    public void setTitre(String v)          { this.titre = v; }
    public String getPrix()                 { return prix; }
    public void setPrix(String v)           { this.prix = v; }
    public Map<Long, String> getValeursAttributs()       { return valeursAttributs; }
    public void setValeursAttributs(Map<Long, String> v) { this.valeursAttributs = v; }
}
