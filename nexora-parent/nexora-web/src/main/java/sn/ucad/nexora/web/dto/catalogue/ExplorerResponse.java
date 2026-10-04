package sn.ucad.nexora.web.dto.catalogue;

import java.math.BigDecimal;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.ExplorerResponse}. */
public record ExplorerResponse(
        List<CategorieExploree> categories,
        List<Quartier> quartiers,
        List<LieuExplore> lieux,
        List<EspaceSurCarte> espaces) {

    public static ExplorerResponse vide() {
        return new ExplorerResponse(List.of(), List.of(), List.of(), List.of());
    }

    public record CategorieExploree(Long id, String nom, String icone, String couleur, long nombreOffres,
                                    List<SousCategorie> sousCategories) {

        public String initiale() {
            return nom == null || nom.isBlank() ? "?" : nom.substring(0, 1).toUpperCase();
        }
    }

    public record SousCategorie(Long id, String nom, long nombreOffres) {}

    public record Quartier(String commune, String region, long nombreEspaces, long nombreOffres) {}

    public record LieuExplore(Long id, String nom, String typeLieu, String commune,
                              BigDecimal latitude, BigDecimal longitude, long nombreEspaces, double rayonKm) {}

    public record EspaceSurCarte(Long id, String nom, String typeEspace, String commune,
                                 BigDecimal latitude, BigDecimal longitude, long nombreOffres) {}
}
