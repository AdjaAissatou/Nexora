package sn.ucad.nexora.catalogue.application.dto.response;

public class CategorieResponse {
    private Long id;
    private Long idParent;
    private String nom;
    private String description;
    private String icone;
    private String couleur;
    private boolean aDesEnfants;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdParent() { return idParent; }
    public void setIdParent(Long idParent) { this.idParent = idParent; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    public boolean isADesEnfants() { return aDesEnfants; }
    public void setADesEnfants(boolean aDesEnfants) { this.aDesEnfants = aDesEnfants; }
}
