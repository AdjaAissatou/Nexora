package com.nexora.domain.order;

import com.nexora.domain.catalog.Offre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/** Ligne d'une {@link Commande} : une offre, une quantite, un prix fige. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ligne_commande")
public class LigneCommande implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_commande")
    private Long idLigneCommande;

    @Column(name = "quantite", nullable = false)
    private BigDecimal quantite = BigDecimal.ONE;

    /** Prix unitaire fige au moment de la commande. */
    @Column(name = "prix_unitaire", precision = 15, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", precision = 15, scale = 2)
    private BigDecimal sousTotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private Offre offre;
}
