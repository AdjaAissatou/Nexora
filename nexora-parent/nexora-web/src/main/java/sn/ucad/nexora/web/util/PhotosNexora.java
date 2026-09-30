package sn.ucad.nexora.web.util;

import java.util.Map;

/**
 * Photos d'ambiance de la vitrine ({@code resources/images}, voir CREDITS.md). Elles servent de repli
 * quand un espace n'a encore aucune photo : mieux vaut une image évocatrice de son activité qu'un
 * cadre vide. Les chemins sont relatifs à la bibliothèque JSF « images ».
 */
public final class PhotosNexora {

    public static final String RESTAURANTS = "categorie-restaurants.jpg";
    public static final String MODE = "categorie-mode.jpg";
    public static final String MARCHES = "categorie-marches.jpg";
    public static final String SERVICES = "categorie-services.jpg";

    private static final Map<String, String> PAR_TYPE = Map.ofEntries(
            Map.entry("Restaurant", RESTAURANTS),
            Map.entry("Hôtel", RESTAURANTS),
            Map.entry("Boutique", MARCHES),
            Map.entry("Supermarché", MARCHES),
            Map.entry("Pharmacie", MARCHES),
            Map.entry("Atelier", MODE),
            Map.entry("Service", SERVICES),
            Map.entry("Garage", SERVICES),
            Map.entry("Cabinet", SERVICES),
            Map.entry("Agence", SERVICES),
            Map.entry("Clinique", SERVICES),
            Map.entry("École", SERVICES),
            Map.entry("Université", SERVICES),
            Map.entry("Association", SERVICES),
            Map.entry("Administration", SERVICES));

    private PhotosNexora() {}

    public static String pourType(String typeEspace) {
        return typeEspace == null ? SERVICES : PAR_TYPE.getOrDefault(typeEspace, SERVICES);
    }
}
