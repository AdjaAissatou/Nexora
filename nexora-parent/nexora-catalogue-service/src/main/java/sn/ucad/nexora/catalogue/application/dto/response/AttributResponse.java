package sn.ucad.nexora.catalogue.application.dto.response;

import java.util.List;

public class AttributResponse {
    private Long id;
    private String nom;
    private String code;
    private String typeChamp;
    private boolean obligatoire;
    private String unite;
    /** TOUS, PRODUIT ou SERVICE : le formulaire n'affiche que ce qui concerne le type d'offre choisi. */
    private String pourType;
    private List<ValeurAttributResponse> valeurs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTypeChamp() { return typeChamp; }
    public void setTypeChamp(String typeChamp) { this.typeChamp = typeChamp; }
    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public String getPourType() { return pourType; }
    public void setPourType(String pourType) { this.pourType = pourType; }
    public List<ValeurAttributResponse> getValeurs() { return valeurs; }
    public void setValeurs(List<ValeurAttributResponse> valeurs) { this.valeurs = valeurs; }
}
