package sn.ucad.nexora.catalogue.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "utilisateurs")
public class UtilisateurLookupEntity {
    @Id
    @Column(name = "id_utilisateur")
    private Long id;

    @Column(name = "account_id")
    private UUID accountId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
}
