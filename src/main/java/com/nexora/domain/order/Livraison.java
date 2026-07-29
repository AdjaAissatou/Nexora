package com.nexora.domain.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Suivi de livraison associe a une {@link Commande} de produits. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "livraison")
public class Livraison implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livraison")
    private Long idLivraison;

    @Column(name = "transporteur", length = 150)
    private String transporteur;

    @Column(name = "numero_suivi", length = 80)
    private String numeroSuivi;

    @Column(name = "statut_livraison", length = 30)
    private String statutLivraison;

    @Column(name = "date_expedition")
    private LocalDateTime dateExpedition;

    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;

    @Column(name = "frais_livraison", precision = 12, scale = 2)
    private BigDecimal fraisLivraison;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "heure_estimee")
    private LocalDateTime heureEstimee;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande", unique = true)
    private Commande commande;
}
