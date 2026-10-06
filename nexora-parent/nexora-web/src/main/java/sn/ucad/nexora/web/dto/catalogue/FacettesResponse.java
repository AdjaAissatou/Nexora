package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.FacettesResponse}. */
public record FacettesResponse(
        List<Categorie> categories,
        List<Caracteristique> caracteristiques,
        BigDecimal prixMin,
        BigDecimal prixMax) {

    public static FacettesResponse vide() {
        return new FacettesResponse(List.of(), List.of(), null, null);
    }

    public record Categorie(Long id, String nom, long nombre) {}

    public record Caracteristique(Long id, String nom, String typeChamp, List<Valeur> valeurs) {}

    /** {@code ids} : les valeurs de même libellé de plusieurs caractéristiques de même nom. */
    public record Valeur(Long id, String libelle, String couleur, long nombre, java.util.List<Long> ids) {}
}
