package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue d'ensemble pour la page « Explorer » : ce que contient la plateforme, rangé par
 * catégorie, par quartier et autour des lieux connus, plus la carte des espaces.
 * Seules les offres visibles du public sont comptées.
 */
public record ExplorerResponse(
        List<CategorieExploree> categories,
        List<Quartier> quartiers,
        List<LieuExplore> lieux,
        List<EspaceSurCarte> espaces) {

    public record CategorieExploree(Long id, String nom, String icone, String couleur, long nombreOffres,
                                    List<SousCategorie> sousCategories) {}

    public record SousCategorie(Long id, String nom, long nombreOffres) {}

    public record Quartier(String commune, String region, long nombreEspaces, long nombreOffres) {}

    /** Lieu public (marché, gare…) et nombre d'espaces à moins de {@code rayonKm}. */
    public record LieuExplore(Long id, String nom, String typeLieu, String commune,
                              BigDecimal latitude, BigDecimal longitude, long nombreEspaces, double rayonKm) {}

    public record EspaceSurCarte(Long id, String nom, String typeEspace, String commune,
                                 BigDecimal latitude, BigDecimal longitude, long nombreOffres) {}
}
