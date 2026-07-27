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
 * Vue d'administration : indicateurs, validation des espaces et des offres
 * (moderation reelle via {@link AdminService}).
 */
@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private AdminService adminService;

    private AdminStatsDTO stats;
    private List<EspaceAdminDTO> espacesEnAttente;
    private List<OffreDTO> offresEnAttente;

    @PostConstruct
    public void init() { recharger(); }

    private void recharger() {
        this.stats = adminService.statistiquesGlobales();
        this.espacesEnAttente = adminService.espacesEnAttente();
        this.offresEnAttente = adminService.offresEnAttente();
    }

    public void validerEspace(Long id) {
        adminService.validerEspace(id);
        info("Espace validé — désormais vérifié.");
        recharger();
    }

    public void validerOffre(Long id) {
        adminService.validerOffre(id);
        info("Offre validée — publiée et visible dans la recherche.");
        recharger();
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    public AdminStatsDTO getStats()                 { return stats; }
    public List<EspaceAdminDTO> getEspacesEnAttente() { return espacesEnAttente; }
    public List<OffreDTO> getOffresEnAttente()      { return offresEnAttente; }
}
