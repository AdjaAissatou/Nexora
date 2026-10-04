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
        rechercher();
    }

    private void executer() {
        erreur = null;
        determinerLieu();
        try {
            boolean autour = lieuChoisi != null && lieuChoisi.latitude() != null && lieuChoisi.longitude() != null;
            resultats = catalogueApiClient.rechercher(new CritereRecherche(
                    autour ? null : q, null, idCategorie, typeEspace, autour ? null : commune, null, null, null, prixMax, null, null,
                    verifieUniquement ? Boolean.TRUE : null, ouvertMaintenant ? Boolean.TRUE : null, tri, page, TAILLE_PAGE,
                    autour ? lieuChoisi.latitude() : null, autour ? lieuChoisi.longitude() : null, autour ? rayonKm : null));
        } catch (ApiException e) {
            resultats = OffrePageResponse.vide();
            erreur = e.getMessage();
            FacesContext.getCurrentInstance()
                    .addMessage(null, sn.ucad.nexora.web.util.Messages.complet(FacesMessage.SEVERITY_WARN, "Recherche indisponible", erreur));
        } finally {
            recherchee = true;
        }
        chargerLieuxPublics();
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
        if (lieuChoisi == null || lieuChoisi.latitude() == null || lieuChoisi.longitude() == null) return "null";
        return String.format(Locale.ROOT, "{\"lat\":%s,\"lng\":%s,\"rayonKm\":%s}",
                lieuChoisi.latitude(), lieuChoisi.longitude(), rayonKm);
    }

    public boolean isCarteVisible() {
        return lieuChoisi != null || !pointsCarte().isEmpty();
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
}
