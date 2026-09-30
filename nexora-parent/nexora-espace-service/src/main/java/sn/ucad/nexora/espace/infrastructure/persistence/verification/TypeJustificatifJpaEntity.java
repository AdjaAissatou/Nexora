package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.*;

@Entity
@Table(name = "type_justificatif")
public class TypeJustificatifJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_type_justificatif") private Long id;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String libelle;
    private String description;
    @Column(nullable = false) private Boolean actif;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
    public Boolean getActif() { return actif; }
}
