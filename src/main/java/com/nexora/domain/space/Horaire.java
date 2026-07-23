package com.nexora.domain.space;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Creneau d'ouverture d'un {@link EspaceProfessionnel} pour un jour donne.
 * Sert au filtre "ouvert actuellement".
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "horaire")
public class Horaire implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horaire")
    private Long idHoraire;

    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false, length = 12)
    private DayOfWeek jourSemaine;

    @Column(name = "heure_ouverture")
    private LocalTime heureOuverture;

    @Column(name = "heure_fermeture")
    private LocalTime heureFermeture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espace")
    private EspaceProfessionnel espace;
}
