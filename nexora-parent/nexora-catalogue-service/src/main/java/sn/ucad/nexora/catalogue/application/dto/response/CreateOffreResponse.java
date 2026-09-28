package sn.ucad.nexora.catalogue.application.dto.response;

public class CreateOffreResponse {
    private Long id;
    private String titre;

    public CreateOffreResponse() {}

    public CreateOffreResponse(Long id, String titre) {
        this.id = id;
        this.titre = titre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
}
