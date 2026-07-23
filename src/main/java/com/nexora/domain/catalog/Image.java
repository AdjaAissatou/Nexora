package com.nexora.domain.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Media image rattache a une {@link Offre} (galerie, photo principale). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "image")
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_image")
    private Long idImage;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "principale", nullable = false)
    private boolean principale = false;

    @Column(name = "alt", length = 200)
    private String alt;

    @Column(name = "taille")
    private Integer taille;

    @Column(name = "extension", length = 10)
    private String extension;

    @Column(name = "ordre")
    private int ordre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre")
    private Offre offre;
}
