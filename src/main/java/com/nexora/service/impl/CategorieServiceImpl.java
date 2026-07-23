package com.nexora.service.impl;

import com.nexora.dto.CategorieDTO;
import com.nexora.repository.CategorieOffreDao;
import com.nexora.service.CategorieService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

/** Implementation du service des categories. */
@Stateless
public class CategorieServiceImpl implements CategorieService {

    @Inject
    private CategorieOffreDao categorieDao;

    @Override
    public List<CategorieDTO> categoriesRacines() {
        return categorieDao.findVisiblesRacines().stream().map(CategorieDTO::new).toList();
    }
}
