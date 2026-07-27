package com.nexora.web.bean;

import com.nexora.dto.CategorieDTO;
import com.nexora.dto.ref.AttributDTO;
import com.nexora.service.CategorieService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Expose le referentiel reel des categories et de leurs attributs (EAV) aux
 * formulaires, via {@link CategorieService} (base PostgreSQL). Aucune donnee
 * simulee : categories, attributs et valeurs proviennent de la base.
 */
@Named("catalogue")
@RequestScoped
public class CatalogueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private CategorieService categorieService;

    private List<CategorieDTO> categories;

    private List<CategorieDTO> categories() {
        if (categories == null) categories = categorieService.toutesCategories();
        return categories;
    }

    /** Liste deroulante des categories (sous-categories indentees). */
    public List<SelectItem> getCategorieItems() {
        List<SelectItem> items = new ArrayList<>();
        for (CategorieDTO c : categories()) {
            String prefixe = c.getNiveau() > 0 ? "— ".repeat(c.getNiveau()) : "";
            items.add(new SelectItem(c.getId(), prefixe + c.getNom()));
        }
        return items;
    }

    public List<AttributDTO> attributs(Long idCategorie) {
        if (idCategorie == null) return List.of();
        return categorieService.attributsDeCategorie(idCategorie);
    }

    public String nomDe(Long idCategorie) {
        if (idCategorie == null) return "";
        return categories().stream()
                .filter(c -> c.getId().equals(idCategorie))
                .map(CategorieDTO::getNom)
                .findFirst().orElse("");
    }
}
