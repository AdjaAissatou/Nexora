package sn.ucad.nexora.recherche.domain.entity;

import java.time.LocalDateTime;

/**
 * Un favori peut pointer vers une offre OU un espace (l'un des deux est non null).
 */
public class Favori {

    private Long id;
    private Long utilisateurId;
    private Long offreId;    // nullable
    private Long espaceId;   // nullable
    private LocalDateTime dateCreation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
