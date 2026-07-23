package com.nexora.domain.space;

import com.nexora.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Type d'espace professionnel entierement configurable en base
 * (BOUTIQUE, CABINET, CLINIQUE, RESTAURANT, GARAGE, PHARMACIE, ...).
 * Aucun type n'est code en dur : l'administration en cree autant que voulu.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "type_espace_professionnel")
public class TypeEspaceProfessionnel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_espace")
    private Integer idTypeEspace;

    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "libelle", nullable = false, length = 120)
    private String libelle;

    @Column(name = "description", length = 500)
    private String description;
}
