package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_document")
public class VerificationDocumentJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_document") private Long id;
    @Column(name = "id_verification", nullable = false) private Long verificationId;
    @Column(name = "id_type_justificatif", nullable = false) private Long typeJustificatifId;
    @Column(name = "nom_original", nullable = false) private String nomOriginal;
    @Column(name = "chemin_stockage", nullable = false) private String cheminStockage;
    @Column(name = "type_mime", nullable = false) private String typeMime;
    @Column(nullable = false) private Long taille;
    @Column(name = "empreinte_sha256", nullable = false) private String empreinteSha256;
    @Column(nullable = false) private String statut;
    private String motif;
    @Column(name = "id_agent") private Long agentId;
    @Column(name = "date_depot") private LocalDateTime dateDepot;
    @Column(name = "date_examen") private LocalDateTime dateExamen;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }
    public Long getTypeJustificatifId() { return typeJustificatifId; }
    public void setTypeJustificatifId(Long typeJustificatifId) { this.typeJustificatifId = typeJustificatifId; }
    public String getNomOriginal() { return nomOriginal; }
    public void setNomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; }
    public String getCheminStockage() { return cheminStockage; }
    public void setCheminStockage(String cheminStockage) { this.cheminStockage = cheminStockage; }
    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }
    public Long getTaille() { return taille; }
    public void setTaille(Long taille) { this.taille = taille; }
    public String getEmpreinteSha256() { return empreinteSha256; }
    public void setEmpreinteSha256(String empreinteSha256) { this.empreinteSha256 = empreinteSha256; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }
    public LocalDateTime getDateExamen() { return dateExamen; }
    public void setDateExamen(LocalDateTime dateExamen) { this.dateExamen = dateExamen; }
}
