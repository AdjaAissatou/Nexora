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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
import sn.ucad.nexora.web.dto.catalogue.CategorieResponse;
import sn.ucad.nexora.web.dto.catalogue.FacettesResponse;
import sn.ucad.nexora.web.dto.catalogue.LieuPublicResponse;
import sn.ucad.nexora.web.dto.catalogue.OffrePageResponse;
import sn.ucad.nexora.web.dto.catalogue.OffreSummaryResponse;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.util.TypeEspaceVue;

/**
 * Backing bean de {@code recherche.xhtml} — le cœur du parcours utilisateur Nexora :
 * chercher un professionnel ou un établissement, filtrer, et consulter les résultats.
 */
@Named
@ViewScoped
public class RechercheBean implements Serializable {

    @Inject
    private transient CatalogueApiClient catalogueApiClient;

    // Filtres liés au formulaire
    private String q;
    private String typeEspace;
    private Long idCategorie;
    private String commune;
    private BigDecimal prixMax;
    private boolean verifieUniquement;
    /** Uniquement les espaces ouverts en ce moment (§10). */
    private boolean ouvertMaintenant;
    private String tri = "PERTINENCE";

    // Filtres avancés (§17)
    private BigDecimal prixMin;
    /** Note minimale de l'espace : "", "3", "4", "4.5". */
    private String noteMin;
    /** "", "NEUF", "OCCASION". */
    private String etat;
    /** "", "PRODUIT", "SERVICE". */
    private String nature;
    private boolean promo;
    private boolean negociable;
    private boolean domicile;
    /** Valeurs de caractéristiques cochées (taille M, couleur Noir…), par id de valeur. */
    private final Map<Long, Boolean> coches = new java.util.HashMap<>();
    private FacettesResponse facettes = FacettesResponse.vide();
    /** Espaces dont le nom correspond au texte saisi, quels que soient les autres filtres. */
    private List<sn.ucad.nexora.web.dto.catalogue.EspaceTrouveResponse> espacesTrouves = List.of();

    /** « Autour de moi » : position donnée par le navigateur. */
    private BigDecimal maLat;
    private BigDecimal maLng;
    private boolean positionActive;

    /** Tris proposés au-dessus des résultats ; « Plus proches » demande une position ou un lieu. */
    public record Tri(String code, String libelle) implements Serializable {}
    public static final List<Tri> TRIS = List.of(
            new Tri("PERTINENCE", "Pertinence"), new Tri("DISTANCE", "Plus proches"),
            new Tri("PRIX_ASC", "Moins chers"), new Tri("POPULARITE", "Plus vendus"),
            new Tri("NOTE", "Mieux notés"), new Tri("DATE_DESC", "Nouveautés"),
            new Tri("REMISE", "Meilleures remises"), new Tri("PRIX_DESC", "Plus chers"));

    private int page = 0;
    private static final int TAILLE_PAGE = 12;

    private OffrePageResponse resultats = OffrePageResponse.vide();
    private boolean recherchee;
    private String erreur;
    private List<CategorieResponse> categoriesRacines;
    private List<LieuPublicResponse> lieuxPublics = List.of();

    /*
     * Recherche autour d'un lieu (§15) : « sandaga » désigne le Marché Sandaga, pas un article.
     * On affiche alors le lieu (itinéraire) et les commerces autour, du plus proche au plus loin.
     */
    /** Lieu demandé par l'adresse (/recherche?lieu=12), depuis la page Explorer par exemple. */
    private Long lieu;
    /** Texte saisi au moment où le lieu a été choisi : s'il change, le lieu est oublié. */
    private String texteAuChoixDuLieu;
    private LieuPublicResponse lieuChoisi;
    /** Texte qui a fait reconnaître le lieu : une autre saisie annule le lieu. */
    private String texteDuLieu;
    /** Texte pour lequel l'utilisateur a préféré chercher dans les articles plutôt qu'autour du lieu. */
    private String texteSansLieu;
    private double rayonKm = 1;
    public static final List<Double> RAYONS_KM = List.of(0.5, 1.0, 2.0, 5.0);

    @PostConstruct
    public void charger() {
        try {
            categoriesRacines = catalogueApiClient.categoriesRacines();
        } catch (ApiException e) {
            categoriesRacines = List.of();
        }
    }

    public void chargerDepuisParametres() {
        texteAuChoixDuLieu = normaliser(q);
        rechercher();
    }

    public void rechercher() {
        page = 0;
        executer();
    }

    public void pagePrecedente() {
        if (page > 0) {
            page--;
            executer();
        }
    }

    public void pageSuivante() {
        if (!resultats.dernierePage()) {
            page++;
            executer();
        }
    }

    public void choisirType(String nomType) {
        this.typeEspace = nomType;
        rechercher();
    }

    public void reinitialiser() {
        q = null;
        lieu = null;
        lieuChoisi = null;
        texteDuLieu = null;
        texteSansLieu = null;
        typeEspace = null;
        idCategorie = null;
        commune = null;
        prixMax = null;
        verifieUniquement = false;
        ouvertMaintenant = false;
        tri = "PERTINENCE";
        prixMin = null;
        noteMin = null;
        etat = null;
        nature = null;
        promo = false;
        negociable = false;
        domicile = false;
        coches.clear();
        positionActive = false;
        maLat = null;
        maLng = null;
        rechercher();
    }

    /** Tri choisi dans la barre au-dessus des résultats. */
    public void choisirTri(String code) {
        tri = code;
        rechercher();
    }

    /** « Autour de moi » : le navigateur a rempli maLat / maLng. */
    public void autourDeMoi() {
        if (maLat == null || maLng == null) return;
        positionActive = true;
        lieu = null;
        lieuChoisi = null;
        texteDuLieu = null;
        texteSansLieu = normaliser(q);
        rayonKm = 2;
        tri = "DISTANCE";
        rechercher();
    }

    public void quitterPosition() {
        positionActive = false;
        maLat = null;
        maLng = null;
        if ("DISTANCE".equals(tri)) tri = "PERTINENCE";
        rechercher();
    }

    /** Affiner par une catégorie présente dans les résultats. */
    public void affinerCategorie(Long id) {
        idCategorie = id;
        rechercher();
    }

    /** Point autour duquel on cherche : la position de l'utilisateur, sinon le lieu reconnu. */
    private BigDecimal[] point() {
        if (positionActive && maLat != null && maLng != null) return new BigDecimal[] {maLat, maLng};
        if (lieuChoisi != null && lieuChoisi.latitude() != null && lieuChoisi.longitude() != null) {
            return new BigDecimal[] {lieuChoisi.latitude(), lieuChoisi.longitude()};
        }
        return null;
    }

    public boolean isAutourDunPoint() {
        return point() != null;
    }

    private CritereRecherche critere(List<Long> valeurs) {
        BigDecimal[] point = point();
        boolean autourDuLieu = lieuChoisi != null && !positionActive;
        String triEffectif = "DISTANCE".equals(tri) && point == null ? "PERTINENCE" : tri;
        return new CritereRecherche(
                autourDuLieu ? null : q, null, idCategorie, typeEspace, point != null ? null : commune, null, null,
                prixMin, prixMax, "PRODUIT".equals(nature) ? Boolean.TRUE : "SERVICE".equals(nature) ? Boolean.FALSE : null,
                promo ? Boolean.TRUE : null, verifieUniquement ? Boolean.TRUE : null, ouvertMaintenant ? Boolean.TRUE : null,
                triEffectif, page, TAILLE_PAGE,
                point == null ? null : point[0], point == null ? null : point[1], point == null ? null : rayonKm,
                noteMin == null || noteMin.isBlank() ? null : new BigDecimal(noteMin),
                "NEUF".equals(etat) ? Boolean.TRUE : "OCCASION".equals(etat) ? Boolean.FALSE : null,
                negociable ? Boolean.TRUE : null, domicile ? Boolean.TRUE : null, valeurs);
    }

    /** Valeurs cochées encore proposées par les facettes (une valeur absente ne trouverait rien). */
    private List<Long> valeursChoisies() {
        java.util.Set<Long> proposees = new java.util.HashSet<>();
        facettes.caracteristiques().forEach(c -> c.valeurs().forEach(v -> proposees.add(v.id())));
        coches.entrySet().removeIf(e -> !Boolean.TRUE.equals(e.getValue()) || !proposees.contains(e.getKey()));
        return coches.isEmpty() ? null : new ArrayList<>(coches.keySet());
    }

    /** Nombre de filtres actifs (hors texte), affiché sur le bouton « Filtres » en mobile. */
    public int getNombreFiltres() {
        int n = 0;
        for (Object f : new Object[] {idCategorie, vide(typeEspace), vide(commune), prixMin, prixMax, vide(noteMin), vide(etat), vide(nature)}) {
            if (f != null) n++;
        }
        for (boolean b : new boolean[] {verifieUniquement, ouvertMaintenant, promo, negociable, domicile, positionActive}) {
            if (b) n++;
        }
        return n + coches.size();
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /** Racines + la catégorie choisie si c'est une sous-catégorie (sinon la liste la perdrait). */
    public List<CategorieResponse> getCategoriesChoix() {
        if (idCategorie == null || categoriesRacines.stream().anyMatch(c -> c.id().equals(idCategorie))) return categoriesRacines;
        List<CategorieResponse> choix = new ArrayList<>(categoriesRacines);
        facettes.categories().stream().filter(c -> c.id().equals(idCategorie)).findFirst()
                .ifPresent(c -> choix.add(0, new CategorieResponse(c.id(), null, c.nom(), null, null, null, false)));
        return choix;
    }

    private void executer() {
        erreur = null;
        if (positionActive) {
            lieuChoisi = null;
        } else {
            determinerLieu();
        }
        try {
            // Facettes d'abord (sans les caractéristiques cochées) : elles disent quelles valeurs restent possibles
            try {
                facettes = catalogueApiClient.facettes(critere(null));
            } catch (ApiException e) {
                facettes = FacettesResponse.vide();
            }
            resultats = catalogueApiClient.rechercher(critere(valeursChoisies()));
        } catch (ApiException e) {
            resultats = OffrePageResponse.vide();
            erreur = e.getMessage();
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_WARN, "Recherche indisponible", erreur));
        } finally {
            recherchee = true;
        }
        chargerLieuxPublics();
        espacesTrouves = q == null || q.isBlank() ? List.of() : catalogueApiClient.espacesParNom(q);
    }

    /** Lieu désigné par l'adresse ou reconnu dans le texte saisi (« sandaga » → Marché Sandaga). */
    private void determinerLieu() {
        String texte = normaliser(q);
        if (lieu != null && !texte.equals(texteAuChoixDuLieu)) lieu = null;
        if (lieu != null) {
            if (lieuChoisi == null || !lieu.equals(lieuChoisi.id())) {
                try {
                    lieuChoisi = catalogueApiClient.lieuPublic(lieu).orElse(null);
                } catch (ApiException e) {
                    lieuChoisi = null;
                }
                texteDuLieu = null;
            }
            return;
        }
        if (lieuChoisi != null && texteDuLieu != null && texteDuLieu.equals(texte)) return;
        lieuChoisi = null;
        texteDuLieu = null;
        if (texte.length() < 3 || texte.equals(texteSansLieu)) return;
        try {
            for (LieuPublicResponse l : catalogueApiClient.rechercherLieuxPublics(q, null, 5)) {
                if (normaliser(l.nom()).contains(texte)) {
                    lieuChoisi = l;
                    texteDuLieu = texte;
                    return;
                }
            }
        } catch (ApiException e) {
            // Sans lieu reconnu, la recherche reste une recherche de texte.
        }
    }

    private static String normaliser(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    /** Un autre lieu de la liste (« Marché Sandaga » au lieu de « Gare de Sandaga »). */
    public void choisirLieu(Long idLieu) {
        lieu = idLieu;
        texteAuChoixDuLieu = normaliser(q);
        rechercher();
    }

    /** « Chercher plutôt le mot dans les articles » : on oublie le lieu pour ce texte. */
    public void chercherDansLesArticles() {
        lieu = null;
        lieuChoisi = null;
        texteDuLieu = null;
        texteSansLieu = normaliser(q);
        rechercher();
    }

    public void choisirRayon(double rayon) {
        rayonKm = rayon;
        rechercher();
    }

    public LieuPublicResponse getLieuChoisi() {
        return lieuChoisi;
    }

    /** Autres lieux trouvés pour le même texte (pour changer de lieu). */
    public List<LieuPublicResponse> getAutresLieux() {
        if (lieuChoisi == null) return List.of();
        return lieuxPublics.stream().filter(l -> !l.id().equals(lieuChoisi.id())).toList();
    }

    public List<Double> getRayons() {
        return RAYONS_KM;
    }

    public double getRayonKm() {
        return rayonKm;
    }

    public String libelleRayon(double rayon) {
        return rayon < 1 ? Math.round(rayon * 1000) + " m" : (rayon == Math.floor(rayon) ? (long) rayon + " km" : rayon + " km");
    }

    public Long getLieu() {
        return lieu;
    }

    public void setLieu(Long lieu) {
        this.lieu = lieu;
    }

    private void chargerLieuxPublics() {
        if ((q == null || q.isBlank()) && (commune == null || commune.isBlank())) {
            lieuxPublics = lieuChoisi == null ? List.of() : List.of(lieuChoisi);
            return;
        }
        try {
            lieuxPublics = catalogueApiClient.rechercherLieuxPublics(q, lieuChoisi != null ? null : commune, 20);
        } catch (ApiException e) {
            lieuxPublics = List.of();
        }
        if (lieuChoisi != null && lieuxPublics.stream().noneMatch(l -> l.id().equals(lieuChoisi.id()))) {
            List<LieuPublicResponse> avecLieu = new ArrayList<>(lieuxPublics);
            avecLieu.add(0, lieuChoisi);
            lieuxPublics = avecLieu;
        }
    }

    public List<LieuPublicResponse> getLieuxPublics() {
        return lieuxPublics;
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Points géolocalisés (offres + lieux publics) pour la carte de recherche. */
    private List<Map<String, Object>> pointsCarte() {
        List<Map<String, Object>> points = new ArrayList<>();
        for (OffreSummaryResponse o : getContenu()) {
            if (o.latitude() == null || o.longitude() == null) continue;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("lat", o.latitude());
            p.put("lng", o.longitude());
            p.put("nom", o.titre());
            p.put("categorie", "OFFRE");
            p.put("href", "/offre.xhtml?id=" + o.id());
            points.add(p);
        }
        if (positionActive && maLat != null && maLng != null) {
            Map<String, Object> ici = new LinkedHashMap<>();
            ici.put("lat", maLat);
            ici.put("lng", maLng);
            ici.put("nom", "Vous êtes ici");
            ici.put("categorie", "POSITION");
            ici.put("detail", "Votre position");
            points.add(ici);
        }
        for (LieuPublicResponse l : lieuxPublics) {
            if (l.latitude() == null || l.longitude() == null) continue;
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("lat", l.latitude());
            p.put("lng", l.longitude());
            p.put("nom", l.nom());
            p.put("categorie", l.typeLieu());
            p.put("href", String.format(Locale.ROOT,
                    "https://www.google.com/maps/dir/?api=1&destination=%s,%s", l.latitude(), l.longitude()));
            points.add(p);
        }
        return points;
    }

    public String getPointsCarteJson() {
        try {
            return JSON.writeValueAsString(pointsCarte());
        } catch (Exception e) {
            return "[]";
        }
    }

    /** Rayon de recherche à tracer autour du lieu ; « null » hors recherche autour d'un lieu. */
    public String getCercleCarteJson() {
        BigDecimal[] point = point();
        if (point == null) return "null";
        return String.format(Locale.ROOT, "{\"lat\":%s,\"lng\":%s,\"rayonKm\":%s}", point[0], point[1], rayonKm);
    }

    public boolean isCarteVisible() {
        return point() != null || !pointsCarte().isEmpty();
    }

    public List<OffreSummaryResponse> getContenu() {
        return resultats.contenu();
    }

    public long getTotal() {
        return resultats.total();
    }

    public boolean isVide() {
        return recherchee && resultats.contenu().isEmpty();
    }

    public boolean isPagePrecedenteDisponible() {
        return page > 0;
    }

    public boolean isPageSuivanteDisponible() {
        return !resultats.dernierePage();
    }

    public int getPageAffichee() {
        return page + 1;
    }

    public List<TypeEspaceVue> getCategories() {
        return TypeEspaceVue.TOUS;
    }

    public List<CategorieResponse> getCategoriesRacines() {
        return categoriesRacines;
    }

    public Long getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(Long idCategorie) {
        this.idCategorie = idCategorie;
    }

    public String getErreur() {
        return erreur;
    }

    // Accesseurs des filtres

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getTypeEspace() {
        return typeEspace;
    }

    public void setTypeEspace(String typeEspace) {
        this.typeEspace = typeEspace;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public BigDecimal getPrixMax() {
        return prixMax;
    }

    public void setPrixMax(BigDecimal prixMax) {
        this.prixMax = prixMax;
    }

    public boolean isVerifieUniquement() {
        return verifieUniquement;
    }

    public boolean isOuvertMaintenant() {
        return ouvertMaintenant;
    }

    public void setOuvertMaintenant(boolean ouvertMaintenant) {
        this.ouvertMaintenant = ouvertMaintenant;
    }

    public void setVerifieUniquement(boolean verifieUniquement) {
        this.verifieUniquement = verifieUniquement;
    }

    public String getTri() {
        return tri;
    }

    public void setTri(String tri) {
        this.tri = tri;
    }

    // Filtres avancés (§17)

    public List<Tri> getTris() { return TRIS; }
    public FacettesResponse getFacettes() { return facettes; }
    public Map<Long, Boolean> getCoches() { return coches; }
    public BigDecimal getPrixMin() { return prixMin; }
    public void setPrixMin(BigDecimal prixMin) { this.prixMin = prixMin; }
    public String getNoteMin() { return noteMin; }
    public void setNoteMin(String noteMin) { this.noteMin = noteMin; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public String getNature() { return nature; }
    public void setNature(String nature) { this.nature = nature; }
    public boolean isPromo() { return promo; }
    public void setPromo(boolean promo) { this.promo = promo; }
    public boolean isNegociable() { return negociable; }
    public void setNegociable(boolean negociable) { this.negociable = negociable; }
    public boolean isDomicile() { return domicile; }
    public void setDomicile(boolean domicile) { this.domicile = domicile; }
    public BigDecimal getMaLat() { return maLat; }
    public void setMaLat(BigDecimal maLat) { this.maLat = maLat; }
    public BigDecimal getMaLng() { return maLng; }
    public void setMaLng(BigDecimal maLng) { this.maLng = maLng; }
    public boolean isPositionActive() { return positionActive; }

    /** Le flux Découvrir limité à cette recherche (§18) : mêmes critères, une offre à la fois. */
    public String getLienDecouvrir() {
        StringBuilder p = new StringBuilder();
        java.util.function.BiConsumer<String, Object> ajouter = (cle, valeur) -> {
            if (valeur == null || valeur.toString().isBlank() || Boolean.FALSE.equals(valeur)) return;
            p.append(p.length() == 0 ? '?' : '&').append(cle).append('=')
                    .append(java.net.URLEncoder.encode(valeur.toString(), java.nio.charset.StandardCharsets.UTF_8));
        };
        boolean autourDuLieu = lieuChoisi != null && !positionActive;
        ajouter.accept("q", autourDuLieu ? null : q);
        ajouter.accept("idCategorie", idCategorie);
        ajouter.accept("typeEspace", typeEspace);
        ajouter.accept("commune", commune);
        ajouter.accept("prixMin", prixMin);
        ajouter.accept("prixMax", prixMax);
        ajouter.accept("estProduit", "PRODUIT".equals(nature) ? Boolean.TRUE : "SERVICE".equals(nature) ? "false" : null);
        ajouter.accept("avecPromotion", promo);
        ajouter.accept("negociable", negociable);
        ajouter.accept("domicile", domicile);
        ajouter.accept("ouvertMaintenant", ouvertMaintenant);
        ajouter.accept("espaceVerifie", verifieUniquement);
        ajouter.accept("noteMin", noteMin);
        ajouter.accept("neuf", "NEUF".equals(etat) ? Boolean.TRUE : "OCCASION".equals(etat) ? "false" : null);
        coches.forEach((id, coche) -> ajouter.accept("valeurs", Boolean.TRUE.equals(coche) ? id : null));
        // Autour d'un lieu ou de soi : le flux garde la zone (lieu reconnu → recherche de son quartier)
        if (autourDuLieu && lieuChoisi.commune() != null) ajouter.accept("zone", lieuChoisi.commune());
        String chemin = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/decouvrir.xhtml";
        return chemin + p;
    }

    /** Une recherche est en cours : un texte, un lieu ou une position (les filtres seuls ne suffisent pas). */
    public boolean isRechercheEnCours() {
        return (q != null && !q.isBlank()) || lieuChoisi != null || positionActive;
    }

    public List<sn.ucad.nexora.web.dto.catalogue.EspaceTrouveResponse> getEspacesTrouves() {
        return espacesTrouves;
    }
}
