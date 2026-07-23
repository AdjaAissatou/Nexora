package com.nexora.domain.attribute;

import com.nexora.domain.catalog.Offre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Valeur concrete d'un {@link Attribut} pour une {@link Offre} donnee (le "V"
 * du modele EAV). Le stockage est poly-type : selon le {@code Typechamp} de
 * l'attribut, la valeur est portee par la colonne adequate
 * (texte / nombre / date / booleen / longTexte) ou par une reference vers une
 * {@link ValeurAttributPossible} (LIST). Cette approche rend la recherche a
 * facettes indexable sans schema fige.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "offre_attribut",
        indexes = {
                @Index(name = "idx_offreattr_offre", columnList = "id_offre"),
                @Index(name = "idx_offreattr_attribut", columnList = "id_attribut"),
                @Index(name = "idx_offreattr_valtexte", columnList = "valeur_texte"),
                @Index(name = "idx_offreattr_valnombre", columnList = "valeur_nombre")
        })
public class OffreAttribut implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre_attribut")
    private Long idOffreAttribut;

    @Column(name = "valeur_texte", length = 400)
    private String valeurTexte;

    @Column(name = "valeur_nombre", precision = 18, scale = 4)
    private java.math.BigDecimal valeurNombre;

    @Column(name = "valeur_date")
    private LocalDate valeurDate;

    @Column(name = "valeur_boolean")
    private Boolean valeurBoolean;

    @Column(name = "valeur_long_texte", columnDefinition = "text")
    private String valeurLongTexte;

    @Column(name = "ordre_affichage")
    private int ordreAffichage;

    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private Offre offre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_attribut")
    private Attribut attribut;

    /** Renseigne uniquement pour les attributs LIST / MULTI_LIST. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_valeur_attribut")
    private ValeurAttributPossible valeurPossible;
}
