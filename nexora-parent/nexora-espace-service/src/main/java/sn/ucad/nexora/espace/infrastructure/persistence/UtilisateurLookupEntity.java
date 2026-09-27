package sn.ucad.nexora.espace.infrastructure.persistence;
import java.util.UUID; import jakarta.persistence.*;
@Entity @Table(name="utilisateurs") public class UtilisateurLookupEntity{@Id @Column(name="id_utilisateur") private Long id; @Column(name="account_id") private UUID accountId; public Long getId(){return id;} public UUID getAccountId(){return accountId;} public void setId(Long id){this.id=id;} public void setAccountId(UUID v){accountId=v;}}
