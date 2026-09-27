package sn.ucad.nexora.web.util;

import java.util.List;

/**
 * Les 15 types d'espaces de Nexora, tels que seedés dans {@code database/09_seed/04_types_espaces.sql}
 * (icône SVG "trait" dans la palette de l'appli + couleur d'identification de catégorie — jamais la
 * couleur principale de l'app). Le catalogue n'expose pas encore d'endpoint public pour cette liste :
 * elle est donc dupliquée ici, à retirer le jour où {@code GET /api/v1/types-espaces} existera.
 */
public record TypeEspaceVue(String nom, String icone, String couleur) {

    private static String svg(String paths) {
        return "<svg class=\"nx-icon\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" "
                + "stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\">" + paths + "</svg>";
    }

    public static final List<TypeEspaceVue> TOUS = List.of(
            new TypeEspaceVue("Boutique",
                    svg("<path d=\"M6 8h12l-1 12H7L6 8z\"/><path d=\"M9 8V6a3 3 0 0 1 6 0v2\"/>"),
                    "#16a34a"),
            new TypeEspaceVue("Service",
                    svg("<rect x=\"3\" y=\"8\" width=\"18\" height=\"11\" rx=\"2\"/><path d=\"M9 8V6a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2\"/><path d=\"M3 13h18\"/>"),
                    "#0f766e"),
            new TypeEspaceVue("Restaurant",
                    svg("<path d=\"M7 2v6M9 2v6M11 2v6\"/><path d=\"M9 8v13\"/><path d=\"M17 2c1 3-1 5-1 7s1 2 1 2v11\"/>"),
                    "#f59e0b"),
            new TypeEspaceVue("Hôtel",
                    svg("<path d=\"M3 18v-9a2 2 0 0 1 2-2h5v5\"/><path d=\"M3 15h18\"/><path d=\"M21 18v-4a2 2 0 0 0-2-2h-8\"/><path d=\"M3 18v3M21 18v3\"/>"),
                    "#0ea5e9"),
            new TypeEspaceVue("Clinique",
                    svg("<circle cx=\"12\" cy=\"12\" r=\"9\"/><path d=\"M12 8v8M8 12h8\"/>"),
                    "#dc2626"),
            new TypeEspaceVue("Pharmacie",
                    svg("<rect x=\"2\" y=\"9.5\" width=\"20\" height=\"5\" rx=\"2.5\" transform=\"rotate(-45 12 12)\"/><line x1=\"9\" y1=\"9\" x2=\"15\" y2=\"15\" transform=\"rotate(-45 12 12)\"/>"),
                    "#7c3aed"),
            new TypeEspaceVue("Garage",
                    svg("<path d=\"M4 16l1.5-5A2 2 0 0 1 7.4 9.5h9.2A2 2 0 0 1 18.5 11L20 16\"/><rect x=\"3\" y=\"16\" width=\"18\" height=\"4\" rx=\"1.5\"/><circle cx=\"7.5\" cy=\"20\" r=\"1.4\"/><circle cx=\"16.5\" cy=\"20\" r=\"1.4\"/>"),
                    "#ea580c"),
            new TypeEspaceVue("École",
                    svg("<path d=\"M12 6c-2-1.5-5-2-9-1v13c4-1 7-.5 9 1\"/><path d=\"M12 6c2-1.5 5-2 9-1v13c-4-1-7-.5-9 1V6z\"/>"),
                    "#0891b2"),
            new TypeEspaceVue("Université",
                    svg("<path d=\"M12 4 2 9l10 5 10-5-10-5z\"/><path d=\"M6 12v5c0 1.5 3 3 6 3s6-1.5 6-3v-5\"/>"),
                    "#2563eb"),
            new TypeEspaceVue("Supermarché",
                    svg("<circle cx=\"9\" cy=\"20\" r=\"1.3\"/><circle cx=\"17\" cy=\"20\" r=\"1.3\"/><path d=\"M3 4h2l2.4 12.2a2 2 0 0 0 2 1.6h7.2a2 2 0 0 0 2-1.6L21 8H6\"/>"),
                    "#059669"),
            new TypeEspaceVue("Cabinet",
                    svg("<rect x=\"5\" y=\"4\" width=\"14\" height=\"17\" rx=\"2\"/><path d=\"M9 4V3a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v1\"/><path d=\"M9 11h6M9 15h6\"/>"),
                    "#8b5cf6"),
            new TypeEspaceVue("Agence",
                    svg("<rect x=\"4\" y=\"3\" width=\"16\" height=\"18\" rx=\"1\"/><path d=\"M8 7h2M14 7h2M8 11h2M14 11h2M8 15h2M14 15h2\"/><path d=\"M10 21v-4h4v4\"/>"),
                    "#475569"),
            new TypeEspaceVue("Atelier",
                    svg("<path d=\"M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l2.6-2.6a4.5 4.5 0 0 1-6 6l-6.9 6.9a1.7 1.7 0 0 1-2.4-2.4l6.9-6.9a4.5 4.5 0 0 1 6-6l-2.6 2.6z\"/>"),
                    "#b45309"),
            new TypeEspaceVue("Association",
                    svg("<circle cx=\"8.5\" cy=\"8\" r=\"3\"/><circle cx=\"17\" cy=\"9\" r=\"2.5\"/><path d=\"M3 20v-1.5A4.5 4.5 0 0 1 7.5 14h2A4.5 4.5 0 0 1 14 18.5V20\"/><path d=\"M15.5 14.3A4 4 0 0 1 21 18v2\"/>"),
                    "#14b8a6"),
            new TypeEspaceVue("Administration",
                    svg("<path d=\"M4 10l8-6 8 6\"/><path d=\"M4 10h16\"/><path d=\"M6 10v9M10 10v9M14 10v9M18 10v9\"/><path d=\"M3 21h18\"/>"),
                    "#334155"));
}
