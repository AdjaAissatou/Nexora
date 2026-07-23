package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Vue exposee d'une offre (JSF / REST). Decouple la couche presentation du
 * modele JPA (principe de separation des responsabilites, evite l'exposition
 * des entites et les problemes de lazy-loading hors transaction).
 */
@Getter
@Setter
public class OffreDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean disponible;
    private String statut;
    private String categorieNom;
    private String espaceNom;
    private boolean espaceVerifie;
    private Double noteEspace;
    private String imagePrincipale;
    private Double distanceKm;
}
