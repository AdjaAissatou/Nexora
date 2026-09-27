package sn.ucad.nexora.recherche.application.dto.request;

/** Ajout d'une offre ou d'un espace en favori. L'un des deux doit être non null. */
public class AjouterFavoriRequest {
    private Long offreId;
    private Long espaceId;

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
}
