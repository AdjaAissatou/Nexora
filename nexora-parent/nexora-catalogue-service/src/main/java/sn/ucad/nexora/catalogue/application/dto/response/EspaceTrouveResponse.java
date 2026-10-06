package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;

/** Un espace trouvé par son nom dans la recherche (« adjashop » → AdjaShop), même sans offre. */
public record EspaceTrouveResponse(Long id, String nom, String slogan, String logo, String typeEspace, String commune,
                                   String quartier, boolean verifie, boolean certifie, BigDecimal note, Integer nombreAvis,
                                   long nombreOffres, Boolean ouvertMaintenant) {}
