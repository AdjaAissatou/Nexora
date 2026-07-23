package com.nexora.domain.catalog;

import com.nexora.common.entity.BaseEntity;
import com.nexora.domain.attribute.Attribut;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Categorie d'offre hierarchique et entierement configurable. Chaque categorie
 * porte l'ensemble des {@link Attribut} dynamiques qui decrivent les offres qui
 * lui sont rattachees : c'est le pivot de la genericite de la plateforme (tout
 * produit / service s'ajoute sans modifier le code).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categorie_offre",
        indexes = @Index(name = "idx_categorie_slug", columnList = "slug", unique = true))
public class CategorieOffre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long idCategorie;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icone", length = 100)
    private String icone;

    @Column(name = "couleur", length = 20)
    private String couleur;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "slug", unique = true, length = 180)
    private String slug;

    @Column(name = "ordre_affichage")
    private int ordreAffichage;

    @Column(name = "niveau")
    private int niveau;

    @Column(name = "filtrable")
    private Boolean filtrable = Boolean.TRUE;

    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

    @Column(name = "rechercheable")
    private Boolean rechercheable = Boolean.TRUE;

    @Column(name = "populaire")
    private Boolean populaire = Boolean.FALSE;

    /** Arborescence de categories (null = racine). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent")
    private CategorieOffre parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_categorie")
    private TypeCategorie typeCategorie;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
    private List<Attribut> attributs = new ArrayList<>();
}
