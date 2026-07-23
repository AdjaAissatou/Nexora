package com.nexora.domain.user;

import com.nexora.common.entity.BaseEntity;
import com.nexora.common.enums.Genre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Compte utilisateur de la plateforme. Un meme compte peut, selon son
 * {@link Profile}, agir comme visiteur, client, professionnel, livreur,
 * moderateur, etc. Herite des champs d'audit de {@link BaseEntity}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "utilisateur",
        indexes = {
                @Index(name = "idx_utilisateur_email", columnList = "email", unique = true),
                @Index(name = "idx_utilisateur_telephone", columnList = "telephone")
        })
public class Utilisateur extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    @Column(name = "nom", nullable = false, length = 120)
    private String nom;

    @Column(name = "prenom", length = 120)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "telephone", length = 30)
    private String telephone;

    /** Hash BCrypt du mot de passe (jamais le mot de passe en clair). */
    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    /** Compte active/desactive par l'administration. */
    @Column(name = "statut_compte", nullable = false)
    private Boolean statutCompte = Boolean.TRUE;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", length = 20)
    private Genre genre;

    @Column(name = "photo_profil", length = 500)
    private String photoProfil;

    @Column(name = "email_verifie", nullable = false)
    private boolean emailVerifie = false;

    @Column(name = "telephone_verifie", nullable = false)
    private boolean telephoneVerifie = false;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profile")
    private Profile profile;
}
