package com.nexora.domain.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Specialisation "produit" d'une {@link Offre} (bien physique : ordinateur,
 * telephone, voiture, meuble, vetement, immobilier, ...). Ajoute les
 * caracteristiques propres a un bien vendable.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("PRODUIT")
public class Produit extends Offre {

    /** Quantite en stock (null = non gere). */
    @Column(name = "stock")
    private Integer stock;

    /** true = neuf, false = occasion. */
    @Column(name = "neuf")
    private Boolean neuf = Boolean.TRUE;

    @Column(name = "marque", length = 120)
    private String marque;

    @Column(name = "modele", length = 120)
    private String modele;

    @Column(name = "garantie_mois")
    private Integer garantieMois;
}
