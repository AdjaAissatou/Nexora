package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.*;

/** Règle : justificatif demandé pour un type d'espace (NULL = tous) ; un même groupe_alternatif = un seul suffit. */
@Entity
@Table(name = "justificatif_requis")
public class JustificatifRequisJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_justificatif_requis") private Long id;
    @Column(name = "id_type_espace") private Long typeEspaceId;
    @Column(name = "id_type_justificatif", nullable = false) private Long typeJustificatifId;
    @Column(nullable = false) private Boolean obligatoire;
    @Column(name = "groupe_alternatif") private String groupeAlternatif;

    public Long getId() { return id; }
    public Long getTypeEspaceId() { return typeEspaceId; }
    public Long getTypeJustificatifId() { return typeJustificatifId; }
    public Boolean getObligatoire() { return obligatoire; }
    public String getGroupeAlternatif() { return groupeAlternatif; }
}
