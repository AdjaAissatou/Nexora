package sn.ucad.nexora.catalogue.infrastructure.persistence.admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import sn.ucad.nexora.catalogue.application.dto.response.admin.CatalogueAdminDtos.*;

/** Catalogue en SQL natif : arbre des catégories, types d'offre, attributs, valeurs et liens aux types d'espace. */
@Repository
public class CatalogueAdminRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String NOEUD = """
            SELECT c.id_categorie, c.id_categorie_parent, c.nom, c.description, c.icone, c.couleur,
                   COALESCE(c.ordre_affichage, 0), COALESCE(c.actif, TRUE),
                   (SELECT COUNT(*) FROM categorie e WHERE e.id_categorie_parent = c.id_categorie),
                   (SELECT COUNT(*) FROM type_offre t WHERE t.id_categorie = c.id_categorie),
                   (SELECT COUNT(*) FROM attribut a WHERE a.id_categorie = c.id_categorie)
            FROM categorie c
            """;

    // ------------------------------------------------------------------ lecture

    public List<Noeud> enfants(Long parentId) {
        Query q = em.createNativeQuery(NOEUD + (parentId == null ? " WHERE c.id_categorie_parent IS NULL"
                : " WHERE c.id_categorie_parent = :p") + " ORDER BY c.ordre_affichage, c.nom");
        if (parentId != null) q.setParameter("p", parentId);
        return noeuds(q);
    }

    public Optional<Noeud> categorie(Long id) {
        return noeuds(em.createNativeQuery(NOEUD + " WHERE c.id_categorie = :id").setParameter("id", id)).stream().findFirst();
    }

    /** De la racine jusqu'à la catégorie (incluse). */
    @SuppressWarnings("unchecked")
    public List<Etape> chemin(Long id) {
        List<Object[]> r = em.createNativeQuery("""
                WITH RECURSIVE ch(id, parent, nom, actif, niveau) AS (
                    SELECT id_categorie, id_categorie_parent, nom, COALESCE(actif, TRUE), 0 FROM categorie WHERE id_categorie = :id
                    UNION ALL
                    SELECT c.id_categorie, c.id_categorie_parent, c.nom, COALESCE(c.actif, TRUE), ch.niveau + 1
                    FROM categorie c JOIN ch ON c.id_categorie = ch.parent)
                SELECT id, nom, actif FROM ch ORDER BY niveau DESC
                """).setParameter("id", id).getResultList();
        return r.stream().map(l -> new Etape(((Number) l[0]).longValue(), (String) l[1], Boolean.TRUE.equals(l[2]))).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TypeOffreAdmin> types(Long categorieId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT t.id_type_offre, t.libelle, t.description, CAST(t.principale AS TEXT), COALESCE(t.actif, TRUE),
                       (SELECT COUNT(*) FROM offre o WHERE o.id_type_offre = t.id_type_offre AND CAST(o.statut AS TEXT) <> 'SUPPRIME')
                FROM type_offre t WHERE t.id_categorie = :id ORDER BY t.libelle
                """).setParameter("id", categorieId).getResultList();
        return r.stream().map(l -> new TypeOffreAdmin(((Number) l[0]).longValue(), (String) l[1], (String) l[2], (String) l[3],
                Boolean.TRUE.equals(l[4]), ((Number) l[5]).longValue())).toList();
    }

    @SuppressWarnings("unchecked")
    public List<AttributAdmin> attributs(Long categorieId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT a.id_attribut, a.nom, a.code, CAST(a.type_champ AS TEXT), COALESCE(a.obligatoire, FALSE),
                       COALESCE(a.filtrable, TRUE), a.unite, a.aide, COALESCE(a.ordre_affichage, 0), COALESCE(a.actif, TRUE),
                       (SELECT COUNT(*) FROM offre_attribut oa WHERE oa.id_attribut = a.id_attribut)
                FROM attribut a WHERE a.id_categorie = :id ORDER BY a.ordre_affichage, a.nom
                """).setParameter("id", categorieId).getResultList();
        return r.stream().map(l -> {
            long id = ((Number) l[0]).longValue();
            return new AttributAdmin(id, (String) l[1], (String) l[2], (String) l[3], Boolean.TRUE.equals(l[4]),
                    Boolean.TRUE.equals(l[5]), (String) l[6], (String) l[7], ((Number) l[8]).intValue(), Boolean.TRUE.equals(l[9]),
                    ((Number) l[10]).longValue(), valeurs(id));
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public List<ValeurAdmin> valeurs(Long attributId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT v.id_valeur, v.valeur, COALESCE(v.ordre_affichage, 0), COALESCE(v.actif, TRUE),
                       (SELECT COUNT(*) FROM offre_attribut oa WHERE oa.id_valeur = v.id_valeur)
                FROM valeur_attribut_possible v WHERE v.id_attribut = :id ORDER BY v.ordre_affichage, v.valeur
                """).setParameter("id", attributId).getResultList();
        return r.stream().map(l -> new ValeurAdmin(((Number) l[0]).longValue(), (String) l[1], ((Number) l[2]).intValue(),
                Boolean.TRUE.equals(l[3]), ((Number) l[4]).longValue())).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TypeEspaceLien> typesEspace(Long categorieId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT te.id_type_espace, te.nom,
                       EXISTS (SELECT 1 FROM categorie_type_espace x WHERE x.id_type_espace = te.id_type_espace AND x.id_categorie = :id)
                FROM type_espace te ORDER BY te.ordre_affichage, te.nom
                """).setParameter("id", categorieId).getResultList();
        return r.stream().map(l -> new TypeEspaceLien(((Number) l[0]).longValue(), (String) l[1], Boolean.TRUE.equals(l[2]))).toList();
    }

    /** Offres (hors supprimées) de chaque catégorie demandée et de toutes ses sous-catégories. */
    @SuppressWarnings("unchecked")
    public Map<Long, Long> offresParCategorie(List<Long> ids) {
        Map<Long, Long> m = new HashMap<>();
        if (ids.isEmpty()) return m;
        List<Object[]> r = em.createNativeQuery("""
                WITH RECURSIVE f(ancetre, id) AS (
                    SELECT id_categorie, id_categorie FROM categorie WHERE id_categorie IN (:ids)
                    UNION ALL
                    SELECT f.ancetre, c.id_categorie FROM f JOIN categorie c ON c.id_categorie_parent = f.id)
                SELECT f.ancetre, COUNT(o.id_offre) FROM f
                JOIN offre o ON o.id_categorie = f.id AND CAST(o.statut AS TEXT) <> 'SUPPRIME'
                GROUP BY f.ancetre
                """).setParameter("ids", ids).getResultList();
        r.forEach(l -> m.put(((Number) l[0]).longValue(), ((Number) l[1]).longValue()));
        return m;
    }

    /** Catégories et types d'offre dont le nom contient le texte, avec leur chemin. */
    @SuppressWarnings("unchecked")
    public List<Resultat> rechercher(String texte, int limite) {
        List<Object[]> r = em.createNativeQuery("""
                WITH RECURSIVE ch(id, chemin) AS (
                    SELECT id_categorie, CAST(nom AS TEXT) FROM categorie WHERE id_categorie_parent IS NULL
                    UNION ALL
                    SELECT c.id_categorie, ch.chemin || ' › ' || c.nom FROM categorie c JOIN ch ON c.id_categorie_parent = ch.id)
                SELECT * FROM (
                    SELECT 'CATEGORIE', c.id_categorie, c.id_categorie, c.nom, ch.chemin, COALESCE(c.actif, TRUE)
                    FROM categorie c JOIN ch ON ch.id = c.id_categorie WHERE c.nom ILIKE :t
                    UNION ALL
                    SELECT 'TYPE_OFFRE', t.id_type_offre, t.id_categorie, t.libelle, ch.chemin, COALESCE(t.actif, TRUE)
                    FROM type_offre t JOIN ch ON ch.id = t.id_categorie WHERE t.libelle ILIKE :t
                ) x ORDER BY 1, 4 LIMIT :limite
                """).setParameter("t", "%" + texte + "%").setParameter("limite", limite).getResultList();
        return r.stream().map(l -> new Resultat((String) l[0], ((Number) l[1]).longValue(), ((Number) l[2]).longValue(),
                (String) l[3], (String) l[4], Boolean.TRUE.equals(l[5]))).toList();
    }

    public long compter(String sql, Long id) {
        return ((Number) em.createNativeQuery(sql).setParameter("id", id).getSingleResult()).longValue();
    }

    public boolean existe(String sql, Map<String, Object> parametres) {
        Query q = em.createNativeQuery(sql);
        parametres.forEach(q::setParameter);
        return ((Number) q.getSingleResult()).longValue() > 0;
    }

    @SuppressWarnings("unchecked")
    public Optional<Object[]> ligne(String sql, Long id) {
        List<Object[]> r = em.createNativeQuery(sql).setParameter("id", id).getResultList();
        return r.stream().findFirst();
    }

    // ------------------------------------------------------------------ écriture

    /** {@code sql} : un INSERT … RETURNING &lt;identifiant&gt;. */
    public Long inserer(String sql, Map<String, Object> parametres) {
        Query q = em.createNativeQuery(sql);
        parametres.forEach(q::setParameter);
        return ((Number) q.getSingleResult()).longValue();
    }

    public int executer(String sql, Map<String, Object> parametres) {
        Query q = em.createNativeQuery(sql);
        parametres.forEach(q::setParameter);
        return q.executeUpdate();
    }

    public Optional<Long> utilisateurId(UUID compte) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT id_utilisateur FROM utilisateurs WHERE account_id = :c")
                .setParameter("c", compte).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    @SuppressWarnings("unchecked")
    private List<Noeud> noeuds(Query q) {
        List<Object[]> r = q.getResultList();
        List<Long> ids = r.stream().map(l -> ((Number) l[0]).longValue()).toList();
        Map<Long, Long> offres = offresParCategorie(ids);
        return r.stream().map(l -> {
            long id = ((Number) l[0]).longValue();
            return new Noeud(id, l[1] == null ? null : ((Number) l[1]).longValue(), (String) l[2], (String) l[3], (String) l[4],
                    (String) l[5], ((Number) l[6]).intValue(), Boolean.TRUE.equals(l[7]), ((Number) l[8]).longValue(),
                    ((Number) l[9]).longValue(), ((Number) l[10]).longValue(), offres.getOrDefault(id, 0L));
        }).toList();
    }
}
