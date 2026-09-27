package sn.ucad.nexora.recherche.domain.entity;

import java.time.LocalDateTime;

/**
 * Avis laissé par un utilisateur sur une offre ou un espace.
 * Note de 1 à 5.
 */
public class Avis {

    private Long id;
    private Long utilisateurId;
    private Long espaceId;   // nullable
    private Long offreId;    // nullable
    private int note;        // 1..5
    private String commentaire;
    private String reponseFournisseur;
    private LocalDateTime dateCreation;
    private LocalDateTime dateReponse;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public String getReponseFournisseur() { return reponseFournisseur; }
    public void setReponseFournisseur(String reponseFournisseur) { this.reponseFournisseur = reponseFournisseur; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }
}
