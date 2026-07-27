package com.nexora.web.bean;

import com.nexora.dto.OffreDTO;
import com.nexora.service.OffreService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Statistiques reelles de l'espace du fournisseur, calculees a partir de ses
 * offres persistees ({@link OffreService}). Aucune valeur simulee.
 */
@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private SessionBean session;
    @Inject private OffreService offreService;

    private long nbProduits;
    private long nbServices;
    private long nbEnLigne;
    private long nbEnAttente;

    @PostConstruct
    public void init() {
        if (session.getEspaceId() == null) return;
        List<OffreDTO> offres = offreService.offresDeEspace(session.getEspaceId());
        for (OffreDTO o : offres) {
            if ("SERVICE".equals(o.getType())) nbServices++; else nbProduits++;
            if ("PUBLIEE".equals(o.getStatut())) nbEnLigne++;
            else if ("EN_ATTENTE_VALIDATION".equals(o.getStatut())) nbEnAttente++;
        }
    }

    public long getNbOffres()    { return nbProduits + nbServices; }
    public long getNbProduits()  { return nbProduits; }
    public long getNbServices()  { return nbServices; }
    public long getNbEnLigne()   { return nbEnLigne; }
    public long getNbEnAttente() { return nbEnAttente; }
}
