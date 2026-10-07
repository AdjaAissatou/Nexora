package sn.ucad.nexora.catalogue.application.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * « Autre… » à l'ajout d'une offre (docs/architecture-acteurs.md §21) : quand aucune catégorie du rayon
 * ne convient, le professionnel écrit la sienne. L'offre est rangée dans le rayon principal du type de
 * son espace, avec son type « Autre », et garde le texte saisi (offre.categorie_proposee) que
 * l'administration traite ensuite. On ne sort jamais du domaine de l'espace.
 */
@Component
public class RangementAutre {

    /** Où ranger l'offre : catégorie, type d'offre, et nature choisie par le professionnel. */
    public record Rangement(Long idCategorie, Long idTypeOffre, String principale) {}

    public static final int LONGUEUR_MAX = 120;

    private final EntityManager em;

    public RangementAutre(EntityManager em) {
        this.em = em;
    }

    /** Texte proposé nettoyé, ou null si le professionnel n'a pas choisi « Autre… ». */
    public static String texte(String proposee) {
        if (proposee == null || proposee.isBlank()) return null;
        String t = proposee.trim().replaceAll("\\s+", " ");
        if (t.length() < 2) throw new IllegalArgumentException("Précisez la catégorie (deux caractères au moins)");
        if (t.length() > LONGUEUR_MAX) {
            throw new IllegalArgumentException("Catégorie trop longue (" + LONGUEUR_MAX + " caractères au plus)");
        }
        return t;
    }

    public Rangement ranger(Long idEspace, String nature) {
        String principale = "SERVICE".equals(nature) ? "SERVICE" : "PRODUIT";
        // Le rayon de l'espace : celui où il a déjà le plus d'offres (son vrai domaine), sinon un rayon
        // principal de son type d'espace. Toujours parmi les rayons liés à son type d'espace.
        @SuppressWarnings("unchecked")
        List<Object> categories = em.createNativeQuery("""
                WITH RECURSIVE racine_de AS (
                    SELECT id_categorie AS id, id_categorie AS racine FROM categorie WHERE id_categorie_parent IS NULL
                    UNION ALL
                    SELECT c.id_categorie, r.racine FROM categorie c JOIN racine_de r ON c.id_categorie_parent = r.id
                )
                SELECT c.id_categorie
                FROM espace_professionnel ep
                JOIN categorie_type_espace cte ON cte.id_type_espace = ep.id_type_espace
                JOIN categorie c ON c.id_categorie = cte.id_categorie
                WHERE ep.id_espace = :e AND c.id_categorie_parent IS NULL AND COALESCE(c.actif, TRUE)
                ORDER BY (SELECT COUNT(*) FROM offre o JOIN racine_de r ON r.id = o.id_categorie
                          WHERE o.id_espace = :e AND r.racine = c.id_categorie) DESC,
                         cte.principal DESC NULLS LAST, COALESCE(c.ordre_affichage, 0), c.id_categorie
                LIMIT 1
                """).setParameter("e", idEspace).getResultList();
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Aucun rayon n'est encore ouvert pour ce type d'espace : choisissez une catégorie de la liste");
        }
        Long idCategorie = ((Number) categories.get(0)).longValue();
        return new Rangement(idCategorie, typeAutre(idCategorie, principale), principale);
    }

    /** Le type « Autre » à la racine du rayon ; créé la première fois (beaucoup de rayons n'ont de types que dans leurs sous-catégories). */
    private Long typeAutre(Long idCategorie, String principale) {
        @SuppressWarnings("unchecked")
        List<Object> types = em.createNativeQuery("""
                SELECT id_type_offre FROM type_offre
                WHERE id_categorie = :c AND COALESCE(actif, TRUE) AND libelle ~* '^autre'
                ORDER BY (CAST(principale AS TEXT) = :p) DESC, id_type_offre
                LIMIT 1
                """).setParameter("c", idCategorie).setParameter("p", principale).getResultList();
        if (!types.isEmpty()) return ((Number) types.get(0)).longValue();
        return ((Number) em.createNativeQuery("""
                INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
                VALUES (:c, 'Autre', 'Produit ou service non listé — à préciser dans le titre', CAST(:p AS type_offre_principale), TRUE)
                RETURNING id_type_offre
                """).setParameter("c", idCategorie).setParameter("p", principale).getSingleResult()).longValue();
    }
}
