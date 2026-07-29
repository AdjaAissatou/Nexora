package com.nexora.domain.order;

import com.nexora.common.entity.BaseEntity;
import com.nexora.common.enums.StatutCommande;
import com.nexora.domain.payment.ModePaiement;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Commande passee par un client aupres d'un espace professionnel. Couvre a la
 * fois l'achat de produits (avec livraison) et la reservation de prestations
 * (avec date/heure). Herite de l'audit ({@link BaseEntity}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "commande",
        indexes = {
                @Index(name = "idx_commande_numero", columnList = "numero_commande", unique = true),
                @Index(name = "idx_commande_client", columnList = "id_client")
        })
public class Commande extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private Long idCommande;

    @Column(name = "numero_commande", nullable = false, unique = true, length = 40)
    private String numeroCommande;

    @Column(name = "date_commande")
    private LocalDateTime dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_commande", length = 30)
    private StatutCommande statutCommande = StatutCommande.EN_ATTENTE;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "adresse_livraison", length = 400)
    private String adresseLivraison;

    // --- Champs specifiques prestation de service ---
    @Column(name = "date_prestation")
    private LocalDate datePrestation;
    @Column(name = "heure_prestation")
    private LocalTime heurePrestation;
    @Column(name = "statut_prestation", length = 30)
    private String statutPrestation;
    @Column(name = "confirmation_client")
    private boolean confirmationClient = false;

    // --- Relations ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_client")
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_espace")
    private EspaceProfessionnel espace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mode_paiement")
    private ModePaiement modePaiement;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueStatutCommande> historique = new ArrayList<>();

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Livraison livraison;
}
