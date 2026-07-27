package com.nexora.domain.catalog;

import com.nexora.common.entity.BaseEntity;
import com.nexora.common.enums.StatutOffre;
import com.nexora.domain.attribute.OffreAttribut;
import com.nexora.domain.space.EspaceProfessionnel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Offre : element central du catalogue, racine de la hierarchie
 * {@link Produit} / {@link ServicePro} (strategie {@code SINGLE_TABLE},
 * discriminateur {@code type_offre_dtype}). Une offre appartient a un espace,
 * une categorie, et porte ses valeurs d'attributs dynamiques
 * ({@link OffreAttribut}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "offre",
        indexes = {
                @Index(name = "idx_offre_statut", columnList = "statut"),
                @Index(name = "idx_offre_categorie", columnList = "id_categorie"),
                @Index(name = "idx_offre_espace", columnList = "id_espace")
        })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_offre_dtype", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("OFFRE")
public class Offre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long idOffre;

    @Column(name = "titre", nullable = false, length = 220)
    private String titre;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "prix", precision = 15, scale = 2)
    private BigDecimal prix;

    @Column(name = "disponible", nullable = false)
    private boolean disponible = true;

    @Column(name = "negociable")
    private boolean negociable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 30)
    private StatutOffre statut = StatutOffre.BROUILLON;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    /** Motif de rejet renseigne par l'administration lorsque statut = REJETEE. */
    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    // --- Compteurs de reputation / pertinence ---
    @Column(name = "score_pertinence")
    private double scorePertinence = 0d;
    @Column(name = "nb_consultation")
    private Long nbConsultation = 0L;
    @Column(name = "nb_commandes")
    private long nbCommandes = 0L;
    @Column(name = "nb_favoris")
    private long nbFavoris = 0L;

    // --- Relations ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espace")
    private EspaceProfessionnel espace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie")
    private CategorieOffre categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_offre")
    private TypeOffre typeOffre;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OffreAttribut> attributs = new ArrayList<>();
}
