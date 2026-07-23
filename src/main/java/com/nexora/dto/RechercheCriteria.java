package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Critere de recherche multi-facettes transmis du front (JSF ou REST) au
 * moteur de recherche. Regroupe l'ensemble des filtres exiges par le cahier des
 * charges : mot-cle, categorie, prix, note, geolocalisation, etat, promotion,
 * disponibilite, ainsi que les attributs dynamiques (EAV) via {@code attributs}.
 */
@Getter
@Setter
public class RechercheCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String motCle;
    private Long idCategorie;
    private Long idTypeOffre;         // PRODUIT / SERVICE
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private Integer noteMin;

    // --- Geolocalisation ---
    private Double latitude;
    private Double longitude;
    private Double distanceMaxKm;
    private String ville;
    private String quartier;
    private String region;
    private String pays;

    // --- Filtres booleens ---
    private Boolean disponibleUniquement;
    private Boolean enPromotion;
    private Boolean neufUniquement;
    private Boolean livraisonDisponible;
    private Boolean ouvertMaintenant;
    private Boolean professionnelVerifie;
    private Boolean professionnelCertifie;

    private String marque;

    /** Filtres sur attributs dynamiques : idAttribut -> valeur recherchee. */
    private Map<Long, String> attributs = new HashMap<>();

    /** Tri : PROXIMITE, PRIX_ASC, PRIX_DESC, NOTE, POPULARITE, RECENT, PERTINENCE, ALPHABETIQUE. */
    private String tri = "PERTINENCE";

    private int page = 0;
    private int taillePage = 20;
}
