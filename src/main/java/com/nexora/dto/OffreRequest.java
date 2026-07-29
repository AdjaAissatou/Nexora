package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Charge utile de publication d'une offre (produit ou service) par un
 * fournisseur. Les caracteristiques dynamiques sont transmises dans
 * {@code attributsTexte} (idAttribut -> valeur), sans schema fige.
 */
@Getter
@Setter
public class OffreRequest implements Serializable {

    private String type;            // PRODUIT | SERVICE
    private String titre;
    private String description;
    private BigDecimal prix;
    private boolean disponible = true;
    private boolean negociable;
    private Long idEspace;
    private Long idCategorie;
    private Long idTypeOffre;

    // Produit
    private String marque;
    private String modele;
    private Boolean neuf;
    private Integer stock;

    // Service
    private String typeService;
    private BigDecimal tarif;
    private Boolean aDomicile;

    // Photos de l'offre (URLs). La premiere devient l'image principale.
    private List<String> images;

    // Attributs dynamiques (EAV)
    private Map<Long, String> attributsTexte;
}
