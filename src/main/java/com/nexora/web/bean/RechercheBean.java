package com.nexora.web.bean;

import com.nexora.dto.OffreDTO;
import com.nexora.dto.PageResult;
import com.nexora.dto.RechercheCriteria;
import com.nexora.service.OffreService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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

    private RechercheCriteria criteria = new RechercheCriteria();
    private List<OffreDTO> resultats = List.of();
    private long total;

    @PostConstruct
    public void init() {
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
}
