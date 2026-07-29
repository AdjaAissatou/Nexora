package com.nexora.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Permission fine (ex: {@code OFFRE_CREER}, {@code COMMANDE_VALIDER}) attribuee
 * a un role. Brique de base du controle d'acces (RBAC).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permission")
    private Long idPermission;

    @Column(name = "libelle", nullable = false, unique = true, length = 120)
    private String libelle;

    @Column(name = "description", length = 255)
    private String description;
}
