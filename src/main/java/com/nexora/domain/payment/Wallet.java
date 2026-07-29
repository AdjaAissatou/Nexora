package com.nexora.domain.payment;

import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Portefeuille electronique d'un {@link Utilisateur} (solde + solde bloque escrow). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wallet")
    private Long idWallet;

    @Column(name = "solde_disponible", precision = 15, scale = 2)
    private BigDecimal soldeDisponible = BigDecimal.ZERO;

    @Column(name = "solde_bloque", precision = 15, scale = 2)
    private BigDecimal soldeBloque = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur", unique = true)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();
}
