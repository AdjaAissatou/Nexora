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
    private String photoApercu;

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
        photoApercu = null;
        if (photo == null || photo.getSize() == 0) { rechercher(); return; }
        try {
            byte[] bytes = photo.getInputStream().readAllBytes();
            photoApercu = vignetteDataUri(bytes);
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

    /** Miniature JPEG (max 96 px) encodee en data URI, pour l'apercu dans le bandeau. */
    private static String vignetteDataUri(byte[] bytes) {
        try {
            java.awt.image.BufferedImage src =
                    javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
            if (src == null) return null;
            int max = 96;
            int w = src.getWidth(), h = src.getHeight();
            double r = Math.min((double) max / w, (double) max / h);
            int nw = Math.max(1, (int) Math.round(w * r));
            int nh = Math.max(1, (int) Math.round(h * r));
            java.awt.image.BufferedImage dst =
                    new java.awt.image.BufferedImage(nw, nh, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = dst.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, nw, nh, null);
            g.dispose();
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(dst, "jpeg", out);
            return "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            return null;
        }
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
    public String getPhotoApercu()     { return photoApercu; }
}
