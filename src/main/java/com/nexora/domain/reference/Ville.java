package com.nexora.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Ville/commune rattachee a une {@link Region}. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ville",
        indexes = @Index(name = "idx_ville_region", columnList = "id_region"))
public class Ville implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ville")
    private Long idVille;

    @Column(name = "nom", nullable = false, length = 120)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_region")
    private Region region;
}
