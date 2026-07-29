package com.nexora.domain.review;

import com.nexora.domain.catalog.Offre;
import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Mise en favori d'une {@link Offre} par un {@link Utilisateur}. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "favori",
        uniqueConstraints = @UniqueConstraint(name = "uk_favori", columnNames = {"id_utilisateur", "id_offre"}))
public class Favori implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favori")
    private Long idFavori;

    @Column(name = "date_favori")
    private LocalDateTime dateFavori;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private Offre offre;
}
