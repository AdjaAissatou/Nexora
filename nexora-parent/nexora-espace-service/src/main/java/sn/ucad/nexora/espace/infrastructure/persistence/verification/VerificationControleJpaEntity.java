package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_controle")
public class VerificationControleJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_controle") private Long id;
    @Column(name = "id_verification", nullable = false) private Long verificationId;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String resultat;
    private String commentaire;
    @Column(name = "id_agent") private Long agentId;
    @Column(name = "date_controle") private LocalDateTime dateControle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public LocalDateTime getDateControle() { return dateControle; }
    public void setDateControle(LocalDateTime dateControle) { this.dateControle = dateControle; }
}
