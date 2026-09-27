package sn.ucad.nexora.recherche.application.dto.request;

/** Enregistrement d'une consultation d'offre ou d'espace. */
public class EnregistrerConsultationRequest {
    private Long offreId;    // l'un des deux doit être non null
    private Long espaceId;
    private Integer dureeSecondes;

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public Integer getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(Integer dureeSecondes) { this.dureeSecondes = dureeSecondes; }
}
