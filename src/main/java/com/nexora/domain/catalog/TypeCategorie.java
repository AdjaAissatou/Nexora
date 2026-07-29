package com.nexora.domain.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Type de categorie d'offre, configurable (ex: ELECTRONIQUE, IMMOBILIER,
 * SANTE, TRANSPORT). Permet de grouper les categories par famille metier.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "type_categorie")
public class TypeCategorie implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_categorie")
    private Integer idTypeCategorie;

    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "libelle", nullable = false, length = 120)
    private String libelle;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "actif")
    private Boolean actif = Boolean.TRUE;
}
