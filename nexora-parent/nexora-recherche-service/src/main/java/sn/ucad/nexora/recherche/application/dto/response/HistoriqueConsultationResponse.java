package sn.ucad.nexora.recherche.application.dto.response;

import java.time.LocalDateTime;

public class HistoriqueConsultationResponse {
    private Long id;
    private Long offreId;
    private Long espaceId;
    private LocalDateTime dateConsultation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }
}
