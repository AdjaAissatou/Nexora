package sn.ucad.nexora.web.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import sn.ucad.nexora.web.client.CatalogueApiClient;
import sn.ucad.nexora.web.client.CritereRecherche;
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
    private String commune;
    private BigDecimal prixMax;
    private boolean verifieUniquement;
    private String tri = "PERTINENCE";

    private int page = 0;
    private static final int TAILLE_PAGE = 12;

    private OffrePageResponse resultats = OffrePageResponse.vide();
    private boolean recherchee;
    private String erreur;

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
        commune = null;
        prixMax = null;
        verifieUniquement = false;
        tri = "PERTINENCE";
        rechercher();
    }

    private void executer() {
        erreur = null;
        try {
            resultats = catalogueApiClient.rechercher(new CritereRecherche(
                    q, null, typeEspace, commune, null, null, null, prixMax, null, null,
                    verifieUniquement ? Boolean.TRUE : null, tri, page, TAILLE_PAGE));
        } catch (ApiException e) {
            resultats = OffrePageResponse.vide();
            erreur = e.getMessage();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Recherche indisponible", erreur));
        } finally {
            recherchee = true;
        }
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
