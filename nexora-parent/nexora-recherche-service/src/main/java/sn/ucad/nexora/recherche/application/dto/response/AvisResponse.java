package sn.ucad.nexora.recherche.application.dto.response;

import java.time.LocalDateTime;

public class AvisResponse {
    private Long id;
    private Long utilisateurId;
    private Long offreId;
    private Long espaceId;
    private int note;
    private String commentaire;
    private String reponseFournisseur;
    private LocalDateTime dateCreation;
    private LocalDateTime dateReponse;
    /** Dernière modification par son auteur ; null s'il n'a jamais été modifié. */
    private LocalDateTime dateModification;
    /** Prénom et initiale du nom de l'auteur (« Aïssatou D. »). */
    private String auteur;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
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
    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}
