package com.nexora.domain.dispute;

import com.nexora.domain.order.Commande;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Litige ouvert sur une {@link Commande}, arbitre par l'administration
 * ({@code decisionAdmin}). Declenche la gestion du sequestre (Escrow).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "litige")
public class Litige implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_litige")
    private Long idLitige;

    @Column(name = "motif", length = 200)
    private String motif;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "statut", length = 30)
    private String statut;

    @Column(name = "decision_admin", columnDefinition = "text")
    private String decisionAdmin;

    @Column(name = "date_ouverture")
    private LocalDateTime dateOuverture;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande", unique = true)
    private Commande commande;
}
