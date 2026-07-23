package com.nexora.domain.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Distingue la nature d'une offre : PRODUIT ou SERVICE (configurable). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "type_offre")
public class TypeOffre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_offre")
    private Long idTypeOffre;

    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;
}
