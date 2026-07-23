package com.nexora.domain.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Promotion / coupon applicable a une commande. {@code typeReduction} distingue
 * un pourcentage d'une remise fixe.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion",
        indexes = @Index(name = "idx_promo_code", columnList = "code_promo", unique = true))
public class Promotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promotion")
    private Long idPromotion;

    @Column(name = "code_promo", unique = true, length = 60)
    private String codePromo;

    @Column(name = "valeur", precision = 12, scale = 2)
    private BigDecimal valeur;

    /** POURCENTAGE ou MONTANT_FIXE. */
    @Column(name = "type_reduction", length = 30)
    private String typeReduction;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}
