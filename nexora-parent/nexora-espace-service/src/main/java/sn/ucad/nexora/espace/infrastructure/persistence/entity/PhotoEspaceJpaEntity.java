package sn.ucad.nexora.espace.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_espace")
public class PhotoEspaceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_photo_espace")
    private Long id;

    @Column(name = "id_espace")
    private Long espaceId;

    private String url;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEspaceId() { return espaceId; }
    public void setEspaceId(Long espaceId) { this.espaceId = espaceId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getOrdreAffichage() { return ordreAffichage; }
    public void setOrdreAffichage(Integer ordreAffichage) { this.ordreAffichage = ordreAffichage; }
}
