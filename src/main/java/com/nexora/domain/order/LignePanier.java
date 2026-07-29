package com.nexora.domain.order;

import com.nexora.domain.catalog.Offre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/** Ligne d'un {@link Panier} : une offre et sa quantite souhaitee. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ligne_panier",
        uniqueConstraints = @UniqueConstraint(name = "uk_ligne_panier", columnNames = {"id_panier", "id_offre"}))
public class LignePanier implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_panier")
    private Long idLignePanier;

    @Column(name = "quantite", nullable = false)
    private BigDecimal quantite = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_panier")
    private Panier panier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private Offre offre;
}
