package com.nexora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/** Resultat pagine generique renvoye par les recherches. */
@Getter
@Setter
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> contenu;
    private long total;
    private int page;
    private int taillePage;

    public int getNombrePages() {
        return taillePage == 0 ? 0 : (int) Math.ceil((double) total / taillePage);
    }
}
