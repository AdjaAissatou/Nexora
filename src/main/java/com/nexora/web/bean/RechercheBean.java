package com.nexora.web.bean;

import com.nexora.dto.OffreDTO;
import com.nexora.dto.PageResult;
import com.nexora.dto.RechercheCriteria;
import com.nexora.service.OffreService;
import com.nexora.service.VisionService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.Serializable;
import java.util.List;

/**
 * ManagedBean JSF (MVC2) pilotant la page de recherche. Fait le lien entre la
 * vue PrimeFaces ({@code index.xhtml}) et la couche service, sans logique
 * metier (respect de la separation des responsabilites).
 */
@Named("rechercheBean")
@ViewScoped
public class RechercheBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OffreService offreService;

    @Inject
    private VisionService visionService;

    private RechercheCriteria criteria = new RechercheCriteria();
    private List<OffreDTO> resultats = List.of();
    private long total;
    private transient Part photo;
    private String motCleReconnu;

    @PostConstruct
    public void init() {
        rechercher();
    }

    /**
     * Recherche declenchee par l'upload d'une photo : le mot-cle est deduit par
     * reconnaissance d'image (MobileNetV2 embarque), puis la recherche standard
     * s'execute.
     */
    public void rechercheParPhoto() {
        motCleReconnu = null;
        if (photo == null || photo.getSize() == 0) { rechercher(); return; }
        try {
            byte[] bytes = photo.getInputStream().readAllBytes();
            String motCle = visionService.motCleDepuisImage(bytes);
            if (motCle != null && !motCle.isBlank()) {
                criteria.setMotCle(motCle);
                motCleReconnu = motCle;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO, "Image non reconnue, affinez votre recherche.", null));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN, "Lecture de l'image impossible.", null));
        } finally {
            photo = null;
        }
        rechercher();
    }

    public void rechercher() {
        PageResult<OffreDTO> page = offreService.rechercher(criteria);
        this.resultats = page.getContenu();
        this.total = page.getTotal();
    }

    public void reinitialiser() {
        this.criteria = new RechercheCriteria();
        rechercher();
    }

    // --- Getters / Setters ---
    public RechercheCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(RechercheCriteria criteria) {
        this.criteria = criteria;
    }

    public List<OffreDTO> getResultats() {
        return resultats;
    }

    public long getTotal() {
        return total;
    }

    public String[] getOptionsTri() {
        return new String[]{"PERTINENCE", "PROXIMITE", "PRIX_ASC", "PRIX_DESC",
                "NOTE", "POPULARITE", "RECENT", "ALPHABETIQUE"};
    }

    public Part getPhoto()             { return photo; }
    public void setPhoto(Part photo)   { this.photo = photo; }
    public String getMotCleReconnu()   { return motCleReconnu; }
}
