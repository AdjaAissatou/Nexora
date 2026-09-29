package sn.ucad.nexora.web.util;

import java.util.Map;

/**
 * Vocabulaire de la section "offres" adapté au type d'espace : un professionnel ne pense pas
 * en "offres" mais en "produits" (boutique), "prestations" (garage, service), "plats"
 * (restaurant)... Purement cosmétique (libellés de titres/boutons) — le modèle de données
 * (table {@code offre}) reste unique, seule la présentation change selon
 * {@code type_espace.nom} (seedé dans database/09_seed/04_types_espaces.sql).
 */
public final class VocabulaireOffres {

    public record Vocabulaire(String section, String ajouter, String singulier, String pluriel) {}

    private static final Vocabulaire DEFAUT = new Vocabulaire("Mes offres", "Ajouter une offre", "offre", "offres");

    private static final Map<String, Vocabulaire> PAR_TYPE = Map.ofEntries(
            Map.entry("Boutique", new Vocabulaire("Mes produits", "Ajouter un produit", "produit", "produits")),
            Map.entry("Supermarché", new Vocabulaire("Mes produits", "Ajouter un produit", "produit", "produits")),
            Map.entry("Pharmacie", new Vocabulaire("Mes produits", "Ajouter un produit", "produit", "produits")),
            Map.entry("Service", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Garage", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Cabinet", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Agence", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Atelier", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Association", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Administration", new Vocabulaire("Mes prestations", "Ajouter une prestation", "prestation", "prestations")),
            Map.entry("Restaurant", new Vocabulaire("Mes plats", "Ajouter un plat", "plat", "plats")),
            Map.entry("Hôtel", new Vocabulaire("Mes chambres", "Ajouter une chambre", "chambre", "chambres")),
            Map.entry("Clinique", new Vocabulaire("Mes consultations", "Ajouter une consultation", "consultation", "consultations")),
            Map.entry("École", new Vocabulaire("Mes formations", "Ajouter une formation", "formation", "formations")),
            Map.entry("Université", new Vocabulaire("Mes formations", "Ajouter une formation", "formation", "formations"))
    );

    private VocabulaireOffres() {}

    public static Vocabulaire pour(String nomTypeEspace) {
        if (nomTypeEspace == null) return DEFAUT;
        return PAR_TYPE.getOrDefault(nomTypeEspace, DEFAUT);
    }
}
