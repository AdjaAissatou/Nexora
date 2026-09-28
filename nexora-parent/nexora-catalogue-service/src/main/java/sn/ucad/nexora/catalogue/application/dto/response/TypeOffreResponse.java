package sn.ucad.nexora.catalogue.application.dto.response;

public class TypeOffreResponse {
    private Long id;
    private String libelle;
    private String description;
    private String principale;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPrincipale() { return principale; }
    public void setPrincipale(String principale) { this.principale = principale; }
}
