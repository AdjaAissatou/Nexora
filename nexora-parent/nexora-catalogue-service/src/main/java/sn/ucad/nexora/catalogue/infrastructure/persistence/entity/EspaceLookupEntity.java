package sn.ucad.nexora.catalogue.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "espace_professionnel")
public class EspaceLookupEntity {
    @Id
    @Column(name = "id_espace")
    private Long id;

    @Column(name = "id_utilisateur")
    private Long utilisateurId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
}
