package sn.ucad.nexora.recherche.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signalement")
public class SignalementJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signalement")
    private Long id;

    @Column(name = "id_utilisateur", nullable = false)
    private Long utilisateurId;

    @Column(name = "id_offre")
    private Long offreId;

    @Column(name = "id_espace")
    private Long espaceId;

    @Column(name = "motif")
    private String motif;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // statut_signalement est un ENUM PostgreSQL — on le stocke en String
    @Column(name = "statut", columnDefinition = "statut_signalement")
    private String statut;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
