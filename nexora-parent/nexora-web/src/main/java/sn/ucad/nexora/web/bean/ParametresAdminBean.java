package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import sn.ucad.nexora.web.client.ParametresApiClient;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.Parametre;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.Regle;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.TypeEspace;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.TypeJustificatif;
import sn.ucad.nexora.web.dto.administration.ParametresDtos.VueJustificatifs;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;
import sn.ucad.nexora.web.util.SiteBean;

/**
 * Page {@code admin/parametres.xhtml} (§9.12), deux onglets : les paramètres de Nexora (chaque
 * modification est motivée) et les justificatifs demandés pour la vérification des espaces (types de
 * justificatif, règles générales et règles par type d'espace).
 */
@Named
@ViewScoped
public class ParametresAdminBean implements Serializable {

    private static final Map<String, String> CATEGORIES = Map.of("SITE", "Site public", "COMPTES", "Comptes",
            "ESPACES", "Espaces", "AVIS", "Avis", "VERIFICATION", "Vérification");

    @Inject
    private transient ParametresApiClient client;

    @Inject
    private SessionBean session;

    @Inject
    private SiteBean site;

    private String vue;
    private Long typeEspaceId;
    private String erreur;

    private List<Parametre> parametres = List.of();
    private final Map<String, String> saisies = new HashMap<>();
    private final Map<String, String> motifs = new HashMap<>();

    private VueJustificatifs justificatifs;
    private String motif;
    private Long typeEdite;
    private String typeLibelle;
    private String typeDescription;
    private Long regleJustificatif;
    private boolean regleObligatoire = true;
    private String regleGroupe;
    private final Map<Long, Boolean> obligatoires = new HashMap<>();
    private final Map<Long, String> groupes = new HashMap<>();

    @PostConstruct
    public void charger() {
        Map<String, String> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        vue = "justificatifs".equals(p.get("vue")) ? "justificatifs" : "parametres";
        try {
            typeEspaceId = p.get("typeEspace") == null || p.get("typeEspace").isBlank() ? null : Long.valueOf(p.get("typeEspace"));
        } catch (NumberFormatException e) {
            typeEspaceId = null;
        }
        if (isOngletJustificatifs()) rechargerJustificatifs();
        else rechargerParametres();
    }

    // ------------------------------------------------------------------ paramètres

    private void rechargerParametres() {
        try {
            parametres = client.parametres(session.getAccessToken());
            saisies.clear();
            parametres.forEach(x -> saisies.put(x.code(), affichable(x)));
            erreur = null;
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    /** Paramètres regroupés par catégorie, dans l'ordre du service. */
    public Map<String, List<Parametre>> getParCategorie() {
        Map<String, List<Parametre>> m = new LinkedHashMap<>();
        parametres.stream().sorted(java.util.Comparator.comparing(Parametre::categorie).thenComparing(Parametre::code))
                .forEach(x -> m.computeIfAbsent(x.categorie(), k -> new java.util.ArrayList<>()).add(x));
        return m;
    }

    public List<String> getCategories() {
        return List.copyOf(getParCategorie().keySet());
    }

    public String libelleCategorie(String c) {
        return CATEGORIES.getOrDefault(c, c);
    }

    /** Valeur telle qu'on la lit : « oui/non », nombres sans décimales, « (vide) ». */
    public String valeurLisible(Parametre x) {
        String v = affichable(x);
        if (v == null || v.isBlank()) return "(vide)";
        if ("BOOLEEN".equals(x.genre())) return "true".equals(v) ? "Oui" : "Non";
        return v;
    }

    private static String affichable(Parametre x) {
        String v = x.valeur();
        if (v == null) return "";
        return v.endsWith(".00") ? v.substring(0, v.length() - 3) : v;
    }

    public void enregistrer(String code) {
        executer(() -> {
            client.modifier(session.getAccessToken(), code, saisies.get(code), motifs.get(code));
            motifs.remove(code);
            site.rafraichir();
            rechargerParametres();
            return null;
        }, "Paramètre enregistré. Le site en tient compte dans la minute ; la modification est écrite dans le journal.");
        // Valeur refusée : on réaffiche la valeur en vigueur (le message dit ce qui était attendu).
        if (erreurAction != null) {
            parametres.stream().filter(x -> x.code().equals(code)).findFirst().ifPresent(x -> saisies.put(code, affichable(x)));
        }
    }

    // ------------------------------------------------------------------ justificatifs

    private void rechargerJustificatifs() {
        try {
            appliquer(client.justificatifs(session.getAccessToken()));
            erreur = null;
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    private void appliquer(VueJustificatifs v) {
        justificatifs = v;
        obligatoires.clear();
        groupes.clear();
        v.regles().forEach(r -> {
            obligatoires.put(r.id(), r.obligatoire());
            groupes.put(r.id(), r.groupeAlternatif());
        });
    }

    public List<TypeJustificatif> getTypes() {
        return justificatifs == null ? List.of() : justificatifs.types();
    }

    public List<TypeJustificatif> getTypesActifs() {
        return getTypes().stream().filter(TypeJustificatif::actif).toList();
    }

    public List<TypeEspace> getTypesEspace() {
        return justificatifs == null ? List.of() : justificatifs.typesEspace();
    }

    public List<Regle> getReglesGenerales() {
        return justificatifs == null ? List.of() : justificatifs.regles().stream().filter(r -> r.typeEspaceId() == null).toList();
    }

    /** Règles propres au type d'espace choisi (vide si aucun type n'est choisi). */
    public List<Regle> getReglesDuType() {
        if (justificatifs == null || typeEspaceId == null) return List.of();
        return justificatifs.regles().stream().filter(r -> typeEspaceId.equals(r.typeEspaceId())).toList();
    }

    /** Types d'espace ayant au moins une règle propre, pour les signaler dans la liste. */
    public long reglesPour(Long typeEspace) {
        return justificatifs == null ? 0 : justificatifs.regles().stream().filter(r -> typeEspace.equals(r.typeEspaceId())).count();
    }

    public String getNomTypeEspace() {
        return getTypesEspace().stream().filter(t -> t.id().equals(typeEspaceId)).map(TypeEspace::nom).findFirst().orElse(null);
    }

    public String choisirTypeEspace() {
        return "/admin/parametres?faces-redirect=true&vue=justificatifs" + (typeEspaceId == null ? "" : "&typeEspace=" + typeEspaceId);
    }

    public void editerType(TypeJustificatif t) {
        typeEdite = t.id();
        typeLibelle = t.libelle();
        typeDescription = t.description();
    }

    public void annulerType() {
        typeEdite = null;
        typeLibelle = null;
        typeDescription = null;
    }

    public void enregistrerType() {
        Map<String, Object> corps = new HashMap<>();
        corps.put("libelle", typeLibelle);
        corps.put("description", typeDescription);
        boolean creation = typeEdite == null;
        justificatifsApres(() -> creation
                        ? client.envoyer(session.getAccessToken(), "POST", "/types", corps)
                        : client.envoyer(session.getAccessToken(), "PUT", "/types/{id}", corps, typeEdite),
                creation ? "Type de justificatif créé. Ajoutez-le ci-dessous aux espaces qui doivent le fournir." : "Type de justificatif modifié.");
        if (!FacesContext.getCurrentInstance().isValidationFailed() && erreurAction == null) annulerType();
    }

    public void activerType(Long id, boolean actif) {
        justificatifsApres(() -> client.envoyer(session.getAccessToken(), "POST", "/types/{id}/" + (actif ? "activer" : "desactiver"),
                Map.of("motif", motif == null ? "" : motif), id), actif ? "Type de justificatif réactivé." : "Type de justificatif désactivé.");
    }

    public void ajouterRegle() {
        Map<String, Object> corps = new HashMap<>();
        corps.put("typeEspaceId", typeEspaceId);
        corps.put("typeJustificatifId", regleJustificatif);
        corps.put("obligatoire", regleObligatoire);
        corps.put("groupeAlternatif", regleGroupe);
        justificatifsApres(() -> client.envoyer(session.getAccessToken(), "POST", "/regles", corps),
                "Justificatif ajouté : il est demandé dès maintenant aux professionnels concernés.");
        if (erreurAction == null) {
            regleJustificatif = null;
            regleObligatoire = true;
            regleGroupe = null;
        }
    }

    public void modifierRegle(Long id) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("obligatoire", Boolean.TRUE.equals(obligatoires.get(id)));
        corps.put("groupeAlternatif", groupes.get(id));
        justificatifsApres(() -> client.envoyer(session.getAccessToken(), "PUT", "/regles/{id}", corps, id), "Règle modifiée.");
    }

    public void retirerRegle(Long id) {
        justificatifsApres(() -> client.envoyer(session.getAccessToken(), "POST", "/regles/{id}/supprimer",
                Map.of("motif", motif == null ? "" : motif), id), "Ce justificatif n'est plus demandé.");
    }

    private transient String erreurAction;

    private void justificatifsApres(Supplier<VueJustificatifs> action, String succes) {
        executer(() -> {
            appliquer(action.get());
            motif = null;
            return null;
        }, succes);
    }

    private void executer(Supplier<Void> action, String succes) {
        try {
            action.get();
            erreurAction = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, succes, null));
        } catch (ApiException e) {
            erreurAction = e.getMessage();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
    }

    public boolean isOngletJustificatifs() { return "justificatifs".equals(vue); }
    public String getErreur() { return erreur; }
    public Map<String, String> getSaisies() { return saisies; }
    public Map<String, String> getMotifs() { return motifs; }
    public Long getTypeEspaceId() { return typeEspaceId; }
    public void setTypeEspaceId(Long typeEspaceId) { this.typeEspaceId = typeEspaceId; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public Long getTypeEdite() { return typeEdite; }
    public String getTypeLibelle() { return typeLibelle; }
    public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }
    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }
    public Long getRegleJustificatif() { return regleJustificatif; }
    public void setRegleJustificatif(Long regleJustificatif) { this.regleJustificatif = regleJustificatif; }
    public boolean isRegleObligatoire() { return regleObligatoire; }
    public void setRegleObligatoire(boolean regleObligatoire) { this.regleObligatoire = regleObligatoire; }
    public String getRegleGroupe() { return regleGroupe; }
    public void setRegleGroupe(String regleGroupe) { this.regleGroupe = regleGroupe; }
    public Map<Long, Boolean> getObligatoires() { return obligatoires; }
    public Map<Long, String> getGroupes() { return groupes; }
}
