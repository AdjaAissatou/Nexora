package com.nexora.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Region administrative rattachee a un {@link Pays}. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "region",
        indexes = @Index(name = "idx_region_pays", columnList = "id_pays"))
public class Region implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region")
    private Long idRegion;

    @Column(name = "nom", nullable = false, length = 120)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pays")
    private Pays pays;
}
