package sn.ucad.nexora.catalogue.domain.entity;

public class Service {

    private Long id;
    private Long offreId;
    private Integer dureeEstimee;
    private boolean interventionDomicile;
    private boolean interventionDistance;
    private Integer delaiReponse;
    private boolean reservation;
    private boolean urgence;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
    public boolean isInterventionDomicile() { return interventionDomicile; }
    public void setInterventionDomicile(boolean interventionDomicile) { this.interventionDomicile = interventionDomicile; }
    public boolean isInterventionDistance() { return interventionDistance; }
    public void setInterventionDistance(boolean interventionDistance) { this.interventionDistance = interventionDistance; }
    public Integer getDelaiReponse() { return delaiReponse; }
    public void setDelaiReponse(Integer delaiReponse) { this.delaiReponse = delaiReponse; }
    public boolean isReservation() { return reservation; }
    public void setReservation(boolean reservation) { this.reservation = reservation; }
    public boolean isUrgence() { return urgence; }
    public void setUrgence(boolean urgence) { this.urgence = urgence; }
}
