package com.nexora.domain.space;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Categorie hierarchique d'espace professionnel (arborescence par
 * auto-reference {@code parent}). Ex: Sante > Clinique > Clinique dentaire.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categorie_espace")
public class CategorieEspace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie_espace")
    private Long idCategorieEspace;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icone", length = 100)
    private String icone;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "slug", unique = true, length = 180)
    private String slug;

    @Column(name = "ordre_affichage")
    private int ordreAffichage;

    @Column(name = "niveau")
    private int niveau;

    @Column(name = "actif")
    private Boolean actif = Boolean.TRUE;

    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

    /** Categorie parente (null = racine). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent")
    private CategorieEspace parent;
}
