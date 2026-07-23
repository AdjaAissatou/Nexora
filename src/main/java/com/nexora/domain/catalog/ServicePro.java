package com.nexora.domain.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Specialisation "service" d'une {@link Offre} (prestation : medecin, avocat,
 * plombier, taxi, restaurant, formation, ...). Ajoute les caracteristiques
 * propres a une prestation (tarification, deplacement, delai).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("SERVICE")
public class ServicePro extends Offre {

    @Column(name = "type_service", length = 150)
    private String typeService;

    /** Tarif de reference de la prestation (a l'heure, forfait, ...). */
    @Column(name = "tarif", precision = 15, scale = 2)
    private BigDecimal tarif;

    /** Prestation disponible a la demande. */
    @Column(name = "service_disponible")
    private boolean serviceDisponible = true;

    /** Le prestataire se deplace chez le client. */
    @Column(name = "a_domicile")
    private Boolean aDomicile = Boolean.FALSE;

    /** Delai d'intervention estime en heures. */
    @Column(name = "delai_intervention_h")
    private Integer delaiInterventionHeures;
}
