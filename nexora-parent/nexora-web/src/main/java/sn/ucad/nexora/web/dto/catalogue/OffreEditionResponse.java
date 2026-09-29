package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.OffreEditionResponse}. */
public record OffreEditionResponse(
        Long id,
        Long idEspace,
        Long niveau1Id,
        Long niveau2Id,
        Long niveau3Id,
        Long idCategorie,
        Long idTypeOffre,
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
        List<String> images) {}
