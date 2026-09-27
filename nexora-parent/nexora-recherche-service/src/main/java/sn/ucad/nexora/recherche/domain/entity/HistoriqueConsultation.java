package sn.ucad.nexora.recherche.domain.entity;

import java.time.LocalDateTime;

/**
 * Représente la consultation d'une offre ou d'un espace par un utilisateur.
 */
public class HistoriqueConsultation {

    private Long id;
    private Long utilisateurId;
    private Long offreId;
    private Long espaceId;
    private LocalDateTime dateConsultation;
    private Integer dureeSecondes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }
    public Integer getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(Integer dureeSecondes) { this.dureeSecondes = dureeSecondes; }
}
