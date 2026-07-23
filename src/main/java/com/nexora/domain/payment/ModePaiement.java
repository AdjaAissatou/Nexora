package com.nexora.domain.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Mode de paiement configurable (ESPECES, CARTE, MOBILE_MONEY, WALLET, ...). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mode_paiement")
public class ModePaiement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mode_paiement")
    private Integer idModePaiement;

    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;
}
