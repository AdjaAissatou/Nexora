package com.nexora.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Profil utilisateur (CLIENT, PROFESSIONNEL, LIVREUR, ADMIN, MODERATEUR, ...).
 * Un profil agrege des {@link Role} et est affecte a des {@link Utilisateur}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profile")
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profile")
    private Long idProfile;

    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;

    @Column(name = "actif", nullable = false)
    private Boolean actif = Boolean.TRUE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_profile",
            joinColumns = @JoinColumn(name = "id_profile"),
            inverseJoinColumns = @JoinColumn(name = "id_role"))
    private Set<Role> roles = new HashSet<>();
}
