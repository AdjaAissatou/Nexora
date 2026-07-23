package com.nexora.domain.space;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Certification / agrement d'un espace professionnel, verifiable par un
 * controleur qualite ({@code valide}). Contribue au badge "certifie".
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "certification")
public class Certification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certification")
    private Long idCertification;

    @Column(name = "nom", nullable = false, length = 180)
    private String nom;

    @Column(name = "organisme", length = 180)
    private String organisme;

    @Column(name = "date_obtention")
    private LocalDate dateObtention;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "valide")
    private Boolean valide = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espace")
    private EspaceProfessionnel espace;
}
