package com.nexora.domain.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Statut configurable d'un {@link Paiement} (INITIE, AUTORISE, REGLE, ECHOUE, REMBOURSE). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "statut_paiement")
public class StatutPaiement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_paiement")
    private Integer idStatutPaiement;

    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;
}
