package com.nexora.domain.attribute;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Valeur predefinie d'un {@link Attribut} de type LIST / MULTI_LIST
 * (ex: pour "Couleur" : Rouge, Bleu, Noir). Alimente les listes deroulantes de
 * saisie et les facettes de recherche.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "valeur_attribut_possible")
public class ValeurAttributPossible implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valeur_attribut")
    private Long idValeurAttribut;

    @Column(name = "valeur", nullable = false, length = 200)
    private String valeur;

    @Column(name = "ordre")
    private int ordre;

    @Column(name = "actif")
    private Boolean actif = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_attribut")
    private Attribut attribut;
}
