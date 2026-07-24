package com.nexora.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Pays de reference (alimente les listes deroulantes d'adresse). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pays")
public class Pays implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pays")
    private Long idPays;

    /** Code ISO 3166-1 alpha-2 (SN, ML, CI...). */
    @Column(name = "code", nullable = false, unique = true, length = 2)
    private String code;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    /** Indicatif telephonique (+221...). */
    @Column(name = "indicatif", length = 8)
    private String indicatif;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}
