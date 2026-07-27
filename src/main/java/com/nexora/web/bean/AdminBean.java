package com.nexora.web.bean;

import com.nexora.dto.admin.AdminStatsDTO;
import com.nexora.dto.admin.EspaceAdminDTO;
import com.nexora.dto.admin.LitigeDTO;
import com.nexora.service.AdminService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Vue d'administration : indicateurs et moderation reels ({@link AdminService}). */
@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private AdminService adminService;

    private AdminStatsDTO stats;
    private List<EspaceAdminDTO> enAttente;
    private List<LitigeDTO> litiges;

    @PostConstruct
    public void init() {
        this.stats = adminService.statistiquesGlobales();
        this.enAttente = adminService.espacesEnAttente();
        this.litiges = adminService.litigesOuverts();
    }

    public AdminStatsDTO getStats()            { return stats; }
    public List<EspaceAdminDTO> getEnAttente() { return enAttente; }
    public List<LitigeDTO> getLitiges()        { return litiges; }
}
