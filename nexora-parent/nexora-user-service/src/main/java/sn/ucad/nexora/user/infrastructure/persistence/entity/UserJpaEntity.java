package sn.ucad.nexora.user.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

/**
 * Mappe la vraie table {@code utilisateurs} du schéma métier (celle dont dépendent
 * espace_professionnel, avis, commande, favori, wallet...), et non une table à part : c'était le bug
 * précédent (une table {@code users} créée par Hibernate, jamais reliée au reste du schéma, qui
 * faisait échouer toute lecture côté espace-service avec "Profil utilisateur introuvable").
 *
 * Seuls les champs utiles à user-service aujourd'hui sont mappés ; les colonnes restantes (pseudo,
 * photo_profil, biographie, sexe, langue, statut...) gardent leurs valeurs par défaut en base et ne
 * sont pas touchées par Hibernate tant qu'elles ne sont pas mappées ici.
 */
@Entity
@Table(name = "utilisateurs")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(name = "account_id", unique = true)
    private UUID accountId;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    private String telephone;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    /** Prénom (colonne {@code prenom}) — l'entité expose firstName/lastName pour rester lisible côté Java. */
    public String getFirstName() {
        return prenom;
    }

    public void setFirstName(String firstName) {
        this.prenom = firstName;
    }

    /** Nom de famille (colonne {@code nom}). */
    public String getLastName() {
        return nom;
    }

    public void setLastName(String lastName) {
        this.nom = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return telephone;
    }

    public void setPhone(String phone) {
        this.telephone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return dateCreation;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.dateCreation = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return dateModification;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.dateModification = updatedAt;
    }
}
