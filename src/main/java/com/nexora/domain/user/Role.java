package com.nexora.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role fonctionnel regroupant un ensemble de {@link Permission}. Un role est
 * rattache a un ou plusieurs {@link Profile} via {@code role_profile}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Integer idRole;

    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;

    @Column(name = "date_attribution")
    private LocalDateTime dateAttribution;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "id_role"),
            inverseJoinColumns = @JoinColumn(name = "id_permission"))
    private Set<Permission> permissions = new HashSet<>();
}
