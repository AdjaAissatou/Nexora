package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import sn.ucad.nexora.web.client.CatalogueAdminApiClient;
import sn.ucad.nexora.web.dto.administration.CatalogueAdminDtos.*;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Gestion du catalogue ({@code admin/catalogue.xhtml[?id=]}), §9.11 : sans id, les catégories racines ;
 * avec id, la fiche d'une catégorie (sous-catégories, types d'offre, attributs et valeurs, types
 * d'espace pour une racine). Toute la validation est faite par catalogue-service.
 */
@Named
@ViewScoped
public class CatalogueAdminBean implements Serializable {

    public static final List<String> TYPES_CHAMP = List.of("TEXTE", "NOMBRE", "DATE", "BOOLEAN", "LISTE", "MULTI_LISTE");
    private static final Map<String, String> LIBELLES_CHAMP = Map.of("TEXTE", "Texte", "NOMBRE", "Nombre", "DATE", "Date",
            "BOOLEAN", "Oui / non", "LISTE", "Liste (un choix)", "MULTI_LISTE", "Liste (plusieurs choix)");

    @Inject
    private transient CatalogueAdminApiClient api;

    @Inject
    private SessionBean session;

    private Long id;
    private FicheCategorie fiche;
    private List<Noeud> racines = List.of();
    private String erreur;

    private String recherche;
    private List<Resultat> resultats;

    private String motif;

    // Catégorie courante (modification) et nouvelle catégorie
    private String catNom, catDescription, catIcone, catCouleur;
    private Integer catOrdre;
    private String nouvelleCategorie;

    // Type d'offre (typeEdite null = création)
    private Long typeEdite;
    private String typeLibelle, typeDescription, typePrincipale = "PRODUIT";

    // Attribut (attributEdite null = création)
    private Long attributEdite;
    private String attNom, attType = "TEXTE", attUnite, attAide;
    private boolean attObligatoire, attFiltrable = true;
    private Integer attOrdre;

    /** Valeur à ajouter, par attribut. */
    private Map<Long, String> nouvellesValeurs = new HashMap<>();

    @PostConstruct
    public void charger() {
        String p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        try {
            id = p == null || p.isBlank() ? null : Long.valueOf(p);
        } catch (NumberFormatException e) {
            erreur = "Catégorie introuvable";
            return;
        }
        recharger();
    }

    private void recharger() {
        try {
            if (id == null) {
                racines = api.racines(session.getAccessToken());
            } else {
                appliquer(api.fiche(session.getAccessToken(), id));
            }
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    private void appliquer(FicheCategorie f) {
        fiche = f;
        if (f == null) return;
        Noeud c = f.categorie();
        catNom = c.nom();
        catDescription = c.description();
        catIcone = c.icone();
        catCouleur = c.couleur();
        catOrdre = c.ordre();
    }

    // ------------------------------------------------------------------ recherche

    public void chercher() {
        try {
            resultats = api.rechercher(session.getAccessToken(), recherche);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ catégories

    public String creerCategorie() {
        try {
            FicheCategorie f = api.envoyer(session.getAccessToken(), "POST", "/categories",
                    new CategorieRequest(id, nouvelleCategorie, null, null, null, null));
            flash("Catégorie « " + f.categorie().nom() + " » créée.");
            return "/admin/catalogue?faces-redirect=true&id=" + f.categorie().id();
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
            return null;
        }
    }

    public void modifierCategorie() {
        executer(() -> api.envoyer(session.getAccessToken(), "PUT", "/categories/{id}",
                new CategorieRequest(null, catNom, catDescription, catIcone, catCouleur, catOrdre), id), "Catégorie enregistrée.");
    }

    public void activerCategorie(boolean actif) {
        executer(() -> api.envoyer(session.getAccessToken(), "POST", "/categories/{id}/" + (actif ? "activer" : "desactiver"),
                new MotifRequest(motif), id), actif ? "Catégorie réactivée." : "Catégorie désactivée : elle n'est plus proposée à la création d'offres.");
    }

    public String supprimerCategorie() {
        try {
            Long parent = fiche.categorie().parentId();
            api.envoyer(session.getAccessToken(), "POST", "/categories/{id}/supprimer", new MotifRequest(motif), id);
            flash("Catégorie « " + fiche.categorie().nom() + " » supprimée.");
            return "/admin/catalogue?faces-redirect=true" + (parent == null ? "" : "&id=" + parent);
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
            return null;
        }
    }

    public void lierTypeEspace(Long typeEspace, boolean lier) {
        executer(() -> api.envoyer(session.getAccessToken(), "POST", "/categories/{id}/types-espace/{te}" + (lier ? "" : "/retirer"),
                null, id, typeEspace), lier ? "Catégorie proposée à ce type d'espace." : "Catégorie retirée de ce type d'espace.");
    }

    // ------------------------------------------------------------------ types d'offre

    public void editerType(TypeOffreAdmin t) {
        typeEdite = t.id();
        typeLibelle = t.libelle();
        typeDescription = t.description();
        typePrincipale = t.principale();
    }

    public void annulerType() {
        typeEdite = null;
        typeLibelle = null;
        typeDescription = null;
        typePrincipale = "PRODUIT";
    }

    public void enregistrerType() {
        TypeOffreRequest r = new TypeOffreRequest(typeLibelle, typeDescription, typePrincipale);
        if (executer(() -> typeEdite == null
                ? api.envoyer(session.getAccessToken(), "POST", "/categories/{id}/types", r, id)
                : api.envoyer(session.getAccessToken(), "PUT", "/types/{id}", r, typeEdite), "Type d'offre enregistré.")) {
            annulerType();
        }
    }

    public void actionType(Long typeId, String action) {
        executer(() -> api.envoyer(session.getAccessToken(), "POST", "/types/{id}/" + action, new MotifRequest(motif), typeId),
                libelleAction("Type d'offre", false, action));
    }

    // ------------------------------------------------------------------ attributs et valeurs

    public void editerAttribut(AttributAdmin a) {
        attributEdite = a.id();
        attNom = a.nom();
        attType = a.typeChamp();
        attObligatoire = a.obligatoire();
        attFiltrable = a.filtrable();
        attUnite = a.unite();
        attAide = a.aide();
        attOrdre = a.ordre();
    }

    public void annulerAttribut() {
        attributEdite = null;
        attNom = null;
        attType = "TEXTE";
        attObligatoire = false;
        attFiltrable = true;
        attUnite = null;
        attAide = null;
        attOrdre = null;
    }

    public void enregistrerAttribut() {
        AttributRequest r = new AttributRequest(attNom, attType, attObligatoire, attFiltrable, attUnite, attAide, attOrdre);
        if (executer(() -> attributEdite == null
                ? api.envoyer(session.getAccessToken(), "POST", "/categories/{id}/attributs", r, id)
                : api.envoyer(session.getAccessToken(), "PUT", "/attributs/{id}", r, attributEdite), "Attribut enregistré.")) {
            annulerAttribut();
        }
    }

    public void actionAttribut(Long attributId, String action) {
        executer(() -> api.envoyer(session.getAccessToken(), "POST", "/attributs/{id}/" + action, new MotifRequest(motif), attributId),
                libelleAction("Attribut", false, action));
    }

    public void ajouterValeur(Long attributId) {
        String v = nouvellesValeurs.get(attributId);
        if (executer(() -> api.envoyer(session.getAccessToken(), "POST", "/attributs/{id}/valeurs", new ValeurRequest(v, null), attributId),
                "Valeur ajoutée.")) {
            nouvellesValeurs.remove(attributId);
        }
    }

    public void actionValeur(Long valeurId, String action) {
        executer(() -> api.envoyer(session.getAccessToken(), "POST", "/valeurs/{id}/" + action, new MotifRequest(motif), valeurId),
                libelleAction("Valeur", true, action));
    }

    // ------------------------------------------------------------------ outils

    private boolean executer(Supplier<FicheCategorie> action, String succes) {
        try {
            appliquer(action.get());
            motif = null;
            message(FacesMessage.SEVERITY_INFO, succes);
            return true;
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
            return false;
        }
    }

    private static String libelleAction(String quoi, boolean feminin, String action) {
        String e = feminin ? "e" : "";
        return quoi + switch (action) {
            case "activer" -> " réactivé" + e + ".";
            case "desactiver" -> " désactivé" + e + " : plus proposé" + e + " à la création d'offres.";
            default -> " supprimé" + e + ".";
        };
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(gravite, texte, null));
    }

    private static void flash(String texte) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, texte, null));
    }

    public String libelleChamp(String code) {
        return LIBELLES_CHAMP.getOrDefault(code, code);
    }

    public List<String> getTypesChamp() { return TYPES_CHAMP; }
    public Long getId() { return id; }
    public FicheCategorie getFiche() { return fiche; }
    public List<Noeud> getRacines() { return racines; }
    public String getErreur() { return erreur; }
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    public List<Resultat> getResultats() { return resultats; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getCatNom() { return catNom; }
    public void setCatNom(String v) { this.catNom = v; }
    public String getCatDescription() { return catDescription; }
    public void setCatDescription(String v) { this.catDescription = v; }
    public String getCatIcone() { return catIcone; }
    public void setCatIcone(String v) { this.catIcone = v; }
    public String getCatCouleur() { return catCouleur; }
    public void setCatCouleur(String v) { this.catCouleur = v; }
    public Integer getCatOrdre() { return catOrdre; }
    public void setCatOrdre(Integer v) { this.catOrdre = v; }
    public String getNouvelleCategorie() { return nouvelleCategorie; }
    public void setNouvelleCategorie(String v) { this.nouvelleCategorie = v; }
    public Long getTypeEdite() { return typeEdite; }
    public String getTypeLibelle() { return typeLibelle; }
    public void setTypeLibelle(String v) { this.typeLibelle = v; }
    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String v) { this.typeDescription = v; }
    public String getTypePrincipale() { return typePrincipale; }
    public void setTypePrincipale(String v) { this.typePrincipale = v; }
    public Long getAttributEdite() { return attributEdite; }
    public String getAttNom() { return attNom; }
    public void setAttNom(String v) { this.attNom = v; }
    public String getAttType() { return attType; }
    public void setAttType(String v) { this.attType = v; }
    public boolean isAttObligatoire() { return attObligatoire; }
    public void setAttObligatoire(boolean v) { this.attObligatoire = v; }
    public boolean isAttFiltrable() { return attFiltrable; }
    public void setAttFiltrable(boolean v) { this.attFiltrable = v; }
    public String getAttUnite() { return attUnite; }
    public void setAttUnite(String v) { this.attUnite = v; }
    public String getAttAide() { return attAide; }
    public void setAttAide(String v) { this.attAide = v; }
    public Integer getAttOrdre() { return attOrdre; }
    public void setAttOrdre(Integer v) { this.attOrdre = v; }
    public Map<Long, String> getNouvellesValeurs() { return nouvellesValeurs; }
}
