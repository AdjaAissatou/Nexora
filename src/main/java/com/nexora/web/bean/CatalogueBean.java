package com.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Referentiel des categories et de leurs attributs dynamiques (EAV) pour les
 * formulaires de publication.
 *
 * <p>Les categories sont <b>imbriquees</b> (groupe parent &rarr; sous-categorie)
 * et separees en deux univers : PRODUIT (boutique) et SERVICE (prestataire).
 * Chaque categorie porte ses attributs et leurs valeurs predefinies, pour un
 * remplissage quasi 100 % en listes deroulantes.</p>
 *
 * <p>Jeu de demonstration miroir du prototype. En production, ces donnees
 * proviennent de {@code CategorieService} (base complete des categories /
 * attributs / valeurs) ; l'IHM est identique.</p>
 */
@Named("catalogue")
@ApplicationScoped
public class CatalogueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Long, String> nomCategorie = new LinkedHashMap<>();
    private final Map<Long, List<Attr>> attrsParCategorie = new LinkedHashMap<>();
    private List<SelectItem> itemsProduit;
    private List<SelectItem> itemsService;

    @PostConstruct
    public void init() {
        // --- Univers PRODUIT : groupe -> sous-categories ---
        Map<String, Object[][]> produit = new LinkedHashMap<>();
        produit.put("Mode & Habillement", new Object[][]{{1L, "Vêtements"}, {2L, "Chaussures"}});
        produit.put("Électronique",       new Object[][]{{3L, "Téléphones"}, {4L, "Ordinateurs portables"}});
        produit.put("Véhicules",          new Object[][]{{5L, "Voitures"}});
        produit.put("Immobilier",         new Object[][]{{7L, "Immobilier"}});

        // --- Univers SERVICE ---
        Map<String, Object[][]> service = new LinkedHashMap<>();
        service.put("Restauration & Hôtellerie", new Object[][]{{8L, "Restaurant"}, {9L, "Hôtel"}});
        service.put("Santé",                      new Object[][]{{10L, "Médecin"}});
        service.put("Beauté & Bien-être",         new Object[][]{{11L, "Coiffure & Beauté"}});
        service.put("Bâtiment & Artisanat",       new Object[][]{{12L, "Plomberie"}, {13L, "Électricien"}});

        itemsProduit = construireItems(produit);
        itemsService = construireItems(service);

        // --- Attributs + valeurs predefinies par categorie ---
        attr(1, o("Genre", true, "Homme", "Femme", "Enfant", "Mixte"),
                o("Taille", true, "XS", "S", "M", "L", "XL", "XXL", "3XL"),
                o("Couleur", false, "Blanc", "Noir", "Rouge", "Bleu", "Vert", "Gris", "Rose", "Marron", "Multicolore"),
                o("État", true, "Neuf", "Occasion"));
        attr(3, o("Marque", true, "Samsung", "Apple", "Xiaomi", "Tecno", "Infinix", "Huawei", "Oppo"),
                o("Stockage", true, "16 Go", "32 Go", "64 Go", "128 Go", "256 Go", "512 Go"),
                o("État", true, "Neuf", "Comme neuf", "Occasion", "Reconditionné"));
        attr(5, o("Marque", true, "Toyota", "Mercedes", "Hyundai", "Kia", "Peugeot", "Renault", "Nissan", "BMW"),
                o("Carburant", true, "Essence", "Diesel", "Hybride", "Électrique"),
                o("Boîte de vitesses", true, "Manuelle", "Automatique"));
        attr(7, o("Type de bien", true, "Appartement", "Maison", "Villa", "Terrain", "Studio", "Bureau", "Magasin"),
                o("Transaction", true, "Location", "Vente", "Location courte durée"),
                o("Chambres", false, "1", "2", "3", "4", "5", "6+"));
        attr(8, o("Type de cuisine", true, "Sénégalaise", "Africaine", "Européenne", "Asiatique", "Libanaise", "Fast-food", "Grillades"));
        attr(9, o("Catégorie", true, "1 étoile", "2 étoiles", "3 étoiles", "4 étoiles", "5 étoiles"),
                o("Services", false, "Piscine", "Wifi", "Parking", "Restaurant", "Climatisation"));
        attr(10, o("Spécialité", true, "Généraliste", "Dentiste", "Cardiologue", "Pédiatre", "Gynécologue", "Dermatologue", "Ophtalmologue"),
                 o("Mode de consultation", true, "Au cabinet", "À domicile", "En ligne"));
        attr(11, o("Prestation", true, "Coiffure", "Maquillage", "Manucure", "Soins visage", "Barbier", "Tresses"),
                 o("À domicile", false, "Oui", "Non"));
        attr(12, o("Type d'intervention", true, "Fuite d'eau", "Installation sanitaire", "Débouchage", "Chauffe-eau"),
                 o("Urgence 24/7", false, "Oui", "Non"));
        attr(13, o("Type d'intervention", true, "Installation", "Dépannage", "Mise aux normes", "Domotique"),
                 o("Urgence 24/7", false, "Oui", "Non"));
    }

    private List<SelectItem> construireItems(Map<String, Object[][]> univers) {
        List<SelectItem> items = new ArrayList<>();
        for (Map.Entry<String, Object[][]> g : univers.entrySet()) {
            SelectItemGroup groupe = new SelectItemGroup(g.getKey());
            List<SelectItem> sous = new ArrayList<>();
            for (Object[] c : g.getValue()) {
                Long id = (Long) c[0];
                String nom = (String) c[1];
                nomCategorie.put(id, nom);
                sous.add(new SelectItem(id, nom));
            }
            groupe.setSelectItems(sous.toArray(new SelectItem[0]));
            items.add(groupe);
        }
        return items;
    }

    private void attr(long catId, Attr... attrs) {
        attrsParCategorie.put(catId, List.of(attrs));
    }

    private static Attr o(String nom, boolean obligatoire, String... valeurs) {
        return new Attr(nom, obligatoire, List.of(valeurs));
    }

    // ------------------------------------------------------------- Exposition
    public List<SelectItem> getCategoriesProduit() { return itemsProduit; }
    public List<SelectItem> getCategoriesService() { return itemsService; }

    /** Attributs (avec valeurs) de la categorie choisie ; liste vide sinon. */
    public List<Attr> attributs(Long idCategorie) {
        if (idCategorie == null) return List.of();
        return attrsParCategorie.getOrDefault(idCategorie, List.of());
    }

    public String nomDe(Long idCategorie) {
        return idCategorie == null ? "" : nomCategorie.getOrDefault(idCategorie, "");
    }

    // ============================== Modele ==============================
    public static class Attr implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String nom;
        private final boolean obligatoire;
        private final List<String> valeurs;
        public Attr(String nom, boolean obligatoire, List<String> valeurs) {
            this.nom = nom; this.obligatoire = obligatoire; this.valeurs = valeurs;
        }
        public String getNom()          { return nom; }
        public boolean isObligatoire()  { return obligatoire; }
        public List<String> getValeurs(){ return valeurs; }
    }
}
