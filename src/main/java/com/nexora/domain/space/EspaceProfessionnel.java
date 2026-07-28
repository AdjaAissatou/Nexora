package com.nexora.domain.space;

import com.nexora.common.entity.BaseEntity;
import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Espace professionnel : vitrine d'un vendeur, commercant, artisan,
 * professionnel ou etablissement. Point d'ancrage des offres, adresses,
 * horaires, certifications et conversations. Herite de l'audit
 * ({@link BaseEntity}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "espace_professionnel",
        indexes = {
                @Index(name = "idx_espace_slug", columnList = "slug", unique = true),
                @Index(name = "idx_espace_verifie", columnList = "verifie")
        })
public class EspaceProfessionnel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_espace")
    private Long idEspace;

    @Column(name = "nom_commercial", nullable = false, length = 200)
    private String nomCommercial;

    @Column(name = "slug", unique = true, length = 220)
    private String slug;

    /** Nature de l'espace : BOUTIQUE (produits) | PRESTATAIRE (services) | MIXTE. */
    @Column(name = "nature", length = 20)
    private String nature;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "logo", length = 500)
    private String logo;

    @Column(name = "banniere", length = 500)
    private String banniere;

    // --- Contacts ---
    @Column(name = "telephone_principal", length = 30)
    private String telephonePrincipal;
    @Column(name = "telephone_secondaire", length = 30)
    private String telephoneSecondaire;
    @Column(name = "telephone_tertiaire", length = 30)
    private String telephoneTertiaire;
    @Column(name = "email1", length = 180)
    private String email1;
    @Column(name = "email2", length = 180)
    private String email2;
    @Column(name = "email3", length = 180)
    private String email3;
    @Column(name = "site_web", length = 255)
    private String siteWeb;

    // --- Reseaux sociaux ---
    @Column(name = "facebook", length = 255)
    private String facebook;
    @Column(name = "instagram", length = 255)
    private String instagram;
    @Column(name = "linkedin", length = 255)
    private String linkedin;
    @Column(name = "whatsapp", length = 30)
    private String whatsapp;

    // --- Identifiants legaux ---
    @Column(name = "numero_rccm", length = 80)
    private String numeroRccm;
    @Column(name = "numero_ninea", length = 80)
    private String numeroNinea;
    @Column(name = "numero_fiscal", length = 80)
    private String numeroFiscal;

    // --- Statut & reputation ---
    @Column(name = "verifie", nullable = false)
    private boolean verifie = false;
    @Column(name = "certifie")
    private Boolean certifie = Boolean.FALSE;
    @Column(name = "etat")
    private Boolean etat = Boolean.TRUE;
    @Column(name = "note_moyenne")
    private Double noteMoyenne = 0d;
    @Column(name = "nombre_avis")
    private int nombreAvis = 0;
    @Column(name = "nombre_vues")
    private Long nombreVues = 0L;

    // --- Relations ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proprietaire")
    private Utilisateur proprietaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_espace")
    private TypeEspaceProfessionnel typeEspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie_espace")
    private CategorieEspace categorieEspace;

    @OneToMany(mappedBy = "espace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Adresse> adresses = new ArrayList<>();

    @OneToMany(mappedBy = "espace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Horaire> horaires = new ArrayList<>();

    @OneToMany(mappedBy = "espace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();
}
