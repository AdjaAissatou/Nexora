package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
    private String type;                  // PRODUIT | SERVICE | OFFRE
    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean disponible;
    private String statut;
    private String categorieNom;
    private String espaceNom;
    private boolean espaceVerifie;
    private boolean espaceCertifie;
    private Double noteEspace;
    private int nombreAvis;
    private String imagePrincipale;
    private List<String> galerie;        // toutes les photos de l'offre, ordonnees
    private Double distanceKm;

    // --- Champs de la carte de resultat premium ---
    private boolean ouvert;              // ouvert actuellement (selon horaires)
    private Integer dureeEstimeeMin;     // temps estime jusqu'au lieu (minutes)
    private String telephone;            // pour le bouton Appeler
    private String adresseCourte;        // "Quartier, Ville"
    private Double latitude;             // pour le bouton Itineraire
    private Double longitude;
}
