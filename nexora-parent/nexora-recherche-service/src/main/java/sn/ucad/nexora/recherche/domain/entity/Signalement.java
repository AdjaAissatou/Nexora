package sn.ucad.nexora.recherche.domain.entity;

import java.time.LocalDateTime;

/**
 * Signalement d'une offre ou d'un espace par un utilisateur.
 * Statuts : EN_ATTENTE | EN_COURS | TRAITE | REJETE
 */
public class Signalement {

    private Long id;
    private Long utilisateurId;
    private Long offreId;
    private Long espaceId;
    private String motif;
    private String description;
    private String statut;
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
