package com.nexora.domain.payment;

import com.nexora.domain.order.Commande;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Paiement rattache a une {@link Commande} (relation 1-1). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "paiement")
public class Paiement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long idPaiement;

    @Column(name = "montant", precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "reference_transaction", length = 120)
    private String referenceTransaction;

    @Column(name = "methode_paiement", length = 80)
    private String methodePaiement;

    @Column(name = "devise", length = 8)
    private String devise = "XOF";

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande", unique = true)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_statut_paiement")
    private StatutPaiement statut;
}
