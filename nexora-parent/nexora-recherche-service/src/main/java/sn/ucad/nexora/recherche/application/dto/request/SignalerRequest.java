package sn.ucad.nexora.recherche.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SignalerRequest {
    private Long offreId;
    private Long espaceId;

    @NotBlank
    private String motif;
    private String description;

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
