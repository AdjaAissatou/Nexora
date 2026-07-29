package com.nexora.domain.review;

import com.nexora.common.entity.BaseEntity;
import com.nexora.domain.catalog.Offre;
import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Avis et note (1-5) laisses par un {@link Utilisateur} sur une {@link Offre}.
 * Le professionnel peut y repondre ({@code reponseProfessionnel}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "avis",
        indexes = @Index(name = "idx_avis_offre", columnList = "id_offre"))
public class Avis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Long idAvis;

    @Column(name = "note", nullable = false)
    private int note;

    @Column(name = "commentaire", columnDefinition = "text")
    private String commentaire;

    @Column(name = "date_avis")
    private LocalDateTime dateAvis;

    @Column(name = "reponse_professionnel", columnDefinition = "text")
    private String reponseProfessionnel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur auteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private Offre offre;
}
