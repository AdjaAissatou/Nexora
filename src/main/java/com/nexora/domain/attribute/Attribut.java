package com.nexora.domain.attribute;

import com.nexora.common.entity.BaseEntity;
import com.nexora.domain.catalog.CategorieOffre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition d'une caracteristique dynamique attachee a une
 * {@link CategorieOffre} (ex: "RAM", "Kilometrage", "Superficie", "Pointure").
 * Coeur du modele EAV : ajouter une caracteristique a un domaine metier ne
 * demande aucune modification de code, seulement une insertion en base.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attribut",
        indexes = @Index(name = "idx_attribut_categorie", columnList = "id_categorie"))
public class Attribut extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_attribut")
    private Long idAttribut;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "description", length = 500)
    private String description;

    /** Unite affichee (Go, km, m2, ...). */
    @Column(name = "unite", length = 40)
    private String unite;

    @Column(name = "obligatoire", nullable = false)
    private boolean obligatoire = false;

    /** Utilisable comme filtre de recherche a facettes. */
    @Column(name = "filtrable", nullable = false)
    private boolean filtrable = false;

    /** Mis en avant dans la recherche rapide. */
    @Column(name = "recherche_rapide")
    private Boolean rechercheRapide = Boolean.FALSE;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    /** Categorie qui porte cet attribut. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie")
    private CategorieOffre categorie;

    /** Type de champ (rendu + stockage). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_champ")
    private Typechamp typeChamp;

    /** Valeurs possibles pour les attributs de type LIST / MULTI_LIST. */
    @OneToMany(mappedBy = "attribut", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValeurAttributPossible> valeursPossibles = new ArrayList<>();
}
