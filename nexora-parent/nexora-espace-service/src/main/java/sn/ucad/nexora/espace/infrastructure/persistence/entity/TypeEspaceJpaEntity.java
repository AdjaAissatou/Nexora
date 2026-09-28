package sn.ucad.nexora.espace.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "type_espace")
public class TypeEspaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_espace")
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;
    private String icone;
    private String couleur;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    private Boolean actif = true;

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
    public Boolean getActif() { return actif; }
    public void setActif(Boolean v) { actif = v; }
}
