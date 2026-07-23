package com.nexora.domain.payment;

import com.nexora.domain.order.Commande;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sequestre (escrow) : bloque les fonds d'une {@link Commande} jusqu'a la
 * confirmation de bonne fin, garantissant la transaction entre client et
 * professionnel.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "escrow")
public class Escrow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escrow")
    private Long idEscrow;

    @Column(name = "montant_blocage", precision = 15, scale = 2)
    private BigDecimal montantBlocage;

    @Column(name = "date_blocage")
    private LocalDateTime dateBlocage;

    @Column(name = "date_liberation")
    private LocalDateTime dateLiberation;

    @Column(name = "statut", length = 30)
    private String statut;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande", unique = true)
    private Commande commande;
}
