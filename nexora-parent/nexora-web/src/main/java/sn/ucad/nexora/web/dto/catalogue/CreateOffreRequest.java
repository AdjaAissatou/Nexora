package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.request.CreateOffreRequest}. */
public record CreateOffreRequest(
        Long idEspace,
        Long idTypeOffre,
        Long idCategorie,
        String titre,
        String description,
        BigDecimal prix,
        boolean negociable,
        boolean disponible,
        String marque,
        String modele,
        String reference,
        Integer quantiteStock,
        String garantie,
        Boolean neuf,
        Integer dureeEstimee,
        Boolean interventionDomicile,
        Boolean reservation,
        List<AttributValeurRequest> attributs,
        List<String> images,
        // « Autre… » (§21) : catégorie écrite par le professionnel, nature PRODUIT ou SERVICE
        String categorieProposee,
        String natureProposee) {}
