package sn.ucad.nexora.catalogue.application.dto.response;

public class ValeurAttributResponse {
    private Long id;
    private String valeur;
    /** Pastille (#RRGGBB) pour une couleur, sinon null. */
    private String couleur;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
}
