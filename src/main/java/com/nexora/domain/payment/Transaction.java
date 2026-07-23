package com.nexora.domain.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mouvement financier sur un {@link Wallet} : ventile le montant total entre la
 * part vendeur et la commission plateforme.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transaction")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;

    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "montant_vendeur", precision = 15, scale = 2)
    private BigDecimal montantVendeur;

    @Column(name = "commission_plateforme", precision = 15, scale = 2)
    private BigDecimal commissionPlateforme;

    @Column(name = "type_transaction", length = 40)
    private String typeTransaction;

    @Column(name = "date_transaction")
    private LocalDateTime dateTransaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_wallet")
    private Wallet wallet;
}
