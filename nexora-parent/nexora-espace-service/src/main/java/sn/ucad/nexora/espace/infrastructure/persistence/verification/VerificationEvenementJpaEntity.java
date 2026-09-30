package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnTransformer;

/** Historique d'une demande : insertion seulement, jamais modifié ni supprimé par l'application. */
@Entity
@Table(name = "verification_evenement")
public class VerificationEvenementJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_evenement") private Long id;
    @Column(name = "id_verification", nullable = false) private Long verificationId;
    @Column(nullable = false) private String type;
    @Column(name = "ancien_statut", columnDefinition = "statut_verification") @ColumnTransformer(write = "?::statut_verification") private String ancienStatut;
    @Column(name = "nouveau_statut", columnDefinition = "statut_verification") @ColumnTransformer(write = "?::statut_verification") private String nouveauStatut;
    @Column(name = "id_acteur") private Long acteurId;
    @Column(name = "role_acteur", nullable = false) private String roleActeur;
    private String commentaire;
    @Column(name = "date_evenement") private LocalDateTime dateEvenement;

    public Long getId() { return id; }
    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(String ancienStatut) { this.ancienStatut = ancienStatut; }
    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }
    public Long getActeurId() { return acteurId; }
    public void setActeurId(Long acteurId) { this.acteurId = acteurId; }
    public String getRoleActeur() { return roleActeur; }
    public void setRoleActeur(String roleActeur) { this.roleActeur = roleActeur; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDateTime dateEvenement) { this.dateEvenement = dateEvenement; }
}
