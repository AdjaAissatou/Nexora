package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Facettes d'une recherche (§17) : de quoi affiner les résultats — catégories présentes et
 * valeurs de caractéristiques disponibles (tailles, couleurs, matière…), avec leurs comptes,
 * et la fourchette de prix.
 */
public record FacettesResponse(
        List<Categorie> categories,
        List<Caracteristique> caracteristiques,
        BigDecimal prixMin,
        BigDecimal prixMax) {

    public record Categorie(Long id, String nom, long nombre) {}

    public record Caracteristique(Long id, String nom, String typeChamp, List<Valeur> valeurs) {}

    /**
     * Une valeur proposée. Deux caractéristiques de même nom (« Tailles disponibles » des vêtements
     * homme et femme) sont fusionnées : {@code ids} réunit leurs valeurs « M », {@code id} les représente.
     */
    public record Valeur(Long id, String libelle, String couleur, long nombre, List<Long> ids) {}

    public static FacettesResponse vide() {
        return new FacettesResponse(List.of(), List.of(), null, null);
    }
}
