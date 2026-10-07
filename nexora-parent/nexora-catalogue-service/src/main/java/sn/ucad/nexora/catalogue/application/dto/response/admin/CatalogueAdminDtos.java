package sn.ucad.nexora.catalogue.application.dto.response.admin;

import java.util.List;

/** Requêtes et réponses de la gestion du catalogue (docs/architecture-acteurs.md §9.11). */
public final class CatalogueAdminDtos {

    private CatalogueAdminDtos() {}

    /** Une catégorie dans l'arbre, avec ce qui en dépend. {@code offres} : offres de la catégorie et de ses sous-catégories. */
    public record Noeud(Long id, Long parentId, String nom, String description, String icone, String couleur, int ordre,
                        boolean actif, long sousCategories, long typesOffre, long attributs, long offres) {}

    public record Etape(Long id, String nom, boolean actif) {}

    public record TypeOffreAdmin(Long id, String libelle, String description, String principale, boolean actif, long offres) {}

    public record ValeurAdmin(Long id, String valeur, int ordre, boolean actif, long utilisations) {}

    public record AttributAdmin(Long id, String nom, String code, String typeChamp, boolean obligatoire, boolean filtrable,
                                String unite, String aide, int ordre, boolean actif, long utilisations, List<ValeurAdmin> valeurs) {}

    public record TypeEspaceLien(Long id, String nom, boolean lie) {}

    /** Fiche d'une catégorie : son chemin depuis la racine et tout ce qui s'y rattache. */
    public record FicheCategorie(Noeud categorie, List<Etape> chemin, boolean parentsActifs, List<Noeud> sousCategories,
                                 List<TypeOffreAdmin> typesOffre, List<AttributAdmin> attributs, List<TypeEspaceLien> typesEspace) {}

    public record Resultat(String genre, Long id, Long categorieId, String libelle, String chemin, boolean actif) {}

    /** Une offre rangée en « Autre… » avec la catégorie écrite par son professionnel (§21). */
    public record OffreProposee(Long id, String titre, Long espaceId, String espace, String nature, String texte) {}

    /**
     * Les offres dont la catégorie écrite revient au même (accents, majuscules et pluriels confondus).
     * {@code cle} identifie le groupe dans les actions ; {@code categorieActuelleId} : où elles sont rangées
     * aujourd'hui (le rayon de leur espace), parent proposé par défaut pour la nouvelle catégorie.
     */
    public record Proposition(String cle, String libelle, List<String> variantes, int nombreOffres, int nombreEspaces,
                              Long categorieActuelleId, String categorieActuelle, String nature,
                              java.time.LocalDateTime premiere, List<OffreProposee> offres) {}

    // ------------------------------------------------------------------ requêtes

    /** Créer la catégorie proposée sous {@code parentId}, avec un type d'offre du même nom, et y ranger les offres. */
    public record PropositionCreation(String cle, Long parentId, String nom, String principale) {}

    /** Ranger les offres de la proposition dans une catégorie existante. */
    public record PropositionRattachement(String cle, Long categorieId) {}

    /** Écarter la proposition : les offres restent où elles sont, le texte est effacé. */
    public record PropositionEcart(String cle, String motif) {}

    public record CategorieRequest(Long parentId, String nom, String description, String icone, String couleur, Integer ordre) {}

    public record TypeOffreRequest(String libelle, String description, String principale) {}

    /** {@code obligatoire} : faux par défaut ; {@code filtrable} : vrai par défaut. */
    public record AttributRequest(String nom, String typeChamp, Boolean obligatoire, Boolean filtrable, String unite,
                                  String aide, Integer ordre) {}

    public record ValeurRequest(String valeur, Integer ordre) {}

    public record MotifRequest(String motif) {}
}
