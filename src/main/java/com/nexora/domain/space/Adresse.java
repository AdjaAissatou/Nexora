package com.nexora.domain.space;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Adresse geolocalisee rattachee a un {@link EspaceProfessionnel}. Les champs
 * latitude/longitude alimentent la recherche par distance (Google Maps /
 * OpenStreetMap) et le tri "le plus proche".
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "adresse",
        indexes = @Index(name = "idx_adresse_geo", columnList = "latitude,longitude"))
public class Adresse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adresse")
    private Long idAdresse;

    @Column(name = "pays", length = 100)
    private String pays;

    @Column(name = "region", length = 120)
    private String region;

    @Column(name = "ville", length = 120)
    private String ville;

    @Column(name = "quartier", length = 120)
    private String quartier;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espace")
    private EspaceProfessionnel espace;
}
