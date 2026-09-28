package sn.ucad.nexora.espace.application.dto.response;

public class TypeEspaceResponse {
    private Long id;
    private String nom;
    private String description;
    private String icone;
    private String couleur;
    private Integer ordreAffichage;

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getNom() { return nom; }
    public void setNom(String v) { nom = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getIcone() { return icone; }
    public void setIcone(String v) { icone = v; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String v) { couleur = v; }
    public Integer getOrdreAffichage() { return ordreAffichage; }
    public void setOrdreAffichage(Integer v) { ordreAffichage = v; }
}
