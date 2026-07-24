package com.nexora.dto.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Element generique de liste deroulante (type d'espace, categorie d'espace,
 * mode de paiement, devise, pays...). {@code extra} porte une info secondaire
 * optionnelle (indicatif d'un pays, symbole d'une devise...).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefItemDTO implements Serializable {
    private Long id;
    private String code;
    private String libelle;
    private String extra;

    public RefItemDTO(Long id, String code, String libelle) {
        this(id, code, libelle, null);
    }
}
