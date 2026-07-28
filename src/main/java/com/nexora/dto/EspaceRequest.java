package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Charge utile de creation d'un espace professionnel (boutique, cabinet,
 * clinique, restaurant, garage...) par un utilisateur connecte.
 */
@Getter
@Setter
public class EspaceRequest implements Serializable {

    private String nomCommercial;
    private String nature;          // BOUTIQUE | PRESTATAIRE | MIXTE
    private String description;
    private Integer idTypeEspace;
    private Long idCategorieEspace;

    private String telephonePrincipal;
    private String email1;
    private String siteWeb;
    private String whatsapp;
    private String logo;

    // Adresse principale
    private String pays;
    private String region;
    private String ville;
    private String quartier;
    private Double latitude;
    private Double longitude;
}
