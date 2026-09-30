package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name = "verification_espace")
public class VerificationEspaceJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_verification") private Long id;
    @Column(name = "id_espace", nullable = false) private Long espaceId;
    @Column(name = "id_demandeur", nullable = false) private Long demandeurId;
    @Column(name = "id_agent") private Long agentId;
    @Column(name = "statut", columnDefinition = "statut_verification") @ColumnTransformer(write = "?::statut_verification") private String statut;
    private String motif;
    @Column(name = "date_creation") private LocalDateTime dateCreation;
    @Column(name = "date_soumission") private LocalDateTime dateSoumission;
    @Column(name = "date_prise_en_charge") private LocalDateTime datePriseEnCharge;
    @Column(name = "date_decision") private LocalDateTime dateDecision;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public Long getDemandeurId() { return demandeurId; }
    public void setDemandeurId(Long demandeurId) { this.demandeurId = demandeurId; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }
    public LocalDateTime getDatePriseEnCharge() { return datePriseEnCharge; }
    public void setDatePriseEnCharge(LocalDateTime datePriseEnCharge) { this.datePriseEnCharge = datePriseEnCharge; }
    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }
}
