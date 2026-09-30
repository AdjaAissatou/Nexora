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

    @PostConstruct
    public void charger() {
        try {
            categoriesRacines = catalogueApiClient.categoriesRacines();
        } catch (ApiException e) {
            categoriesRacines = List.of();
        }
    }

    public void chargerDepuisParametres() {
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
        try {
            resultats = catalogueApiClient.rechercher(new CritereRecherche(
                    q, null, idCategorie, typeEspace, commune, null, null, null, prixMax, null, null,
                    verifieUniquement ? Boolean.TRUE : null, ouvertMaintenant ? Boolean.TRUE : null, tri, page, TAILLE_PAGE));
        } catch (ApiException e) {
            resultats = OffrePageResponse.vide();
            erreur = e.getMessage();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Recherche indisponible", erreur));
        } finally {
            recherchee = true;
        }
        chargerLieuxPublics();
    }

    private void chargerLieuxPublics() {
        if ((q == null || q.isBlank()) && (commune == null || commune.isBlank())) {
            lieuxPublics = List.of();
            return;
        }
        try {
            lieuxPublics = catalogueApiClient.rechercherLieuxPublics(q, commune, 20);
        } catch (ApiException e) {
            lieuxPublics = List.of();
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

    public boolean isCarteVisible() {
        return !pointsCarte().isEmpty();
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
