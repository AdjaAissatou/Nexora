package sn.ucad.nexora.web.util;

import java.util.List;

/**
 * Les 15 types d'espaces de Nexora, tels que seedés dans {@code database/09_seed/04_types_espaces.sql}
 * (icône Lucide + couleur d'identification de catégorie — jamais la couleur principale de l'app).
 * Le catalogue n'expose pas encore d'endpoint public pour cette liste : elle est donc dupliquée ici,
 * à retirer le jour où {@code GET /api/v1/types-espaces} existera.
 */
public record TypeEspaceVue(String nom, String emoji, String couleur) {

    public static final List<TypeEspaceVue> TOUS = List.of(
            new TypeEspaceVue("Boutique", "🏪", "#16a34a"),
            new TypeEspaceVue("Service", "💼", "#0f766e"),
            new TypeEspaceVue("Restaurant", "🍽️", "#f59e0b"),
            new TypeEspaceVue("Hôtel", "🏨", "#0ea5e9"),
            new TypeEspaceVue("Clinique", "🏥", "#dc2626"),
            new TypeEspaceVue("Pharmacie", "💊", "#7c3aed"),
            new TypeEspaceVue("Garage", "🚗", "#ea580c"),
            new TypeEspaceVue("École", "🏫", "#0891b2"),
            new TypeEspaceVue("Université", "🎓", "#2563eb"),
            new TypeEspaceVue("Supermarché", "🛒", "#059669"),
            new TypeEspaceVue("Cabinet", "🧑‍💼", "#8b5cf6"),
            new TypeEspaceVue("Agence", "🏢", "#475569"),
            new TypeEspaceVue("Atelier", "🔨", "#b45309"),
            new TypeEspaceVue("Association", "👥", "#14b8a6"),
            new TypeEspaceVue("Administration", "🏛️", "#334155"));
}
