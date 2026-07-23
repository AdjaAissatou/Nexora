package com.nexora.domain.order;

import com.nexora.common.enums.StatutCommande;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Trace horodatee d'un changement de statut d'une {@link Commande} (audit). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historique_statut_commande")
public class HistoriqueStatutCommande implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Long idHistoriqueStatutCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 30)
    private StatutCommande statut;

    @Column(name = "date_changement")
    private LocalDateTime dateChangement;

    @Column(name = "commentaire", length = 300)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande")
    private Commande commande;
}
