package sn.ucad.nexora.recherche.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entité en lecture seule sur la table utilisateurs.
 * Sert uniquement à résoudre le UUID du JWT (account_id) → id_utilisateur (Long).
 */
@Entity
@Table(name = "utilisateurs")
public class UtilisateurLookupEntity {

    @Id
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(name = "account_id")
    private UUID accountId;

    public Long getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public void setId(Long id) { this.id = id; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
}
