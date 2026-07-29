package com.nexora.web.bean;

import com.nexora.dto.OffreDTO;
import com.nexora.dto.admin.AdminStatsDTO;
import com.nexora.dto.admin.EspaceAdminDTO;
import com.nexora.service.AdminService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Vue d'administration : indicateurs, validation / rejet des offres,
 * validation et certification des espaces (moderation reelle).
 */
@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private AdminService adminService;

    private AdminStatsDTO stats;
    private List<EspaceAdminDTO> espacesEnAttente;
    private List<EspaceAdminDTO> espacesVerifies;
    private List<OffreDTO> offresEnAttente;

    // --- Rejet d'une offre ---
    private Long offreARejeterId;
    private String offreARejeterTitre;
    private String motifRejet;

    @PostConstruct
    public void init() { recharger(); }

    private void recharger() {
        this.stats = adminService.statistiquesGlobales();
        this.espacesEnAttente = adminService.espacesEnAttente();
        this.espacesVerifies = adminService.espacesVerifies();
        this.offresEnAttente = adminService.offresEnAttente();
    }

    // ---- Espaces ----
    public void validerEspace(Long id) {
        adminService.validerEspace(id);
        info("Espace validé — désormais vérifié.");
        recharger();
    }

    public void certifier(Long id, boolean certifie) {
        adminService.certifierEspace(id, certifie);
        info(certifie ? "Espace certifié." : "Certification retirée.");
        recharger();
    }

    // ---- Offres ----
    public void validerOffre(Long id) {
        adminService.validerOffre(id);
        info("Offre validée — publiée et visible dans la recherche.");
        recharger();
    }

    public void demarrerRejet(Long id, String titre) {
        this.offreARejeterId = id;
        this.offreARejeterTitre = titre;
        this.motifRejet = null;
    }

    public void annulerRejet() {
        this.offreARejeterId = null;
        this.offreARejeterTitre = null;
        this.motifRejet = null;
    }

    public void confirmerRejet() {
        if (motifRejet == null || motifRejet.isBlank()) {
            avertir("Indiquez le motif du rejet.");
            return;
        }
        adminService.rejeterOffre(offreARejeterId, motifRejet.trim());
        info("Offre rejetée. Le fournisseur verra le motif dans son espace.");
        annulerRejet();
        recharger();
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void avertir(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, m, null));
    }

    public AdminStatsDTO getStats()                   { return stats; }
    public List<EspaceAdminDTO> getEspacesEnAttente() { return espacesEnAttente; }
    public List<EspaceAdminDTO> getEspacesVerifies()  { return espacesVerifies; }
    public List<OffreDTO> getOffresEnAttente()        { return offresEnAttente; }

    public Long getOffreARejeterId()        { return offreARejeterId; }
    public String getOffreARejeterTitre()   { return offreARejeterTitre; }
    public boolean isRejetEnCours()         { return offreARejeterId != null; }
    public String getMotifRejet()           { return motifRejet; }
    public void setMotifRejet(String v)     { this.motifRejet = v; }
}
