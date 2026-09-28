package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.catalogue.application.dto.response.AttributResponse;
import sn.ucad.nexora.catalogue.application.dto.response.CategorieResponse;
import sn.ucad.nexora.catalogue.application.dto.response.TypeOffreResponse;
import sn.ucad.nexora.catalogue.application.dto.response.ValeurAttributResponse;

import java.util.List;

/**
 * Lecture de la hiérarchie de catégories, des types d'offre et des attributs (listes
 * déroulantes) associés — support du formulaire "créer une offre" en cascade.
 * Requêtes natives, cohérent avec le reste du service (pas d'entité JPA pour ces tables).
 */
@Component
public class CategorieQueryRepository {

    private final EntityManager em;

    public CategorieQueryRepository(EntityManager em) {
        this.em = em;
    }

    public List<CategorieResponse> racines() {
        return categories("WHERE c.id_categorie_parent IS NULL AND c.actif = TRUE ORDER BY c.ordre_affichage, c.nom");
    }

    public List<CategorieResponse> enfants(Long idParent) {
        Query q = em.createNativeQuery("""
                SELECT c.id_categorie, c.id_categorie_parent, c.nom, c.description, c.icone, c.couleur,
                       EXISTS(SELECT 1 FROM categorie e WHERE e.id_categorie_parent = c.id_categorie AND e.actif = TRUE)
                FROM categorie c
                WHERE c.id_categorie_parent = :idParent AND c.actif = TRUE
                ORDER BY c.ordre_affichage, c.nom
                """);
        q.setParameter("idParent", idParent);
        return mapCategories(q.getResultList());
    }

    private List<CategorieResponse> categories(String whereOrderBy) {
        Query q = em.createNativeQuery("""
                SELECT c.id_categorie, c.id_categorie_parent, c.nom, c.description, c.icone, c.couleur,
                       EXISTS(SELECT 1 FROM categorie e WHERE e.id_categorie_parent = c.id_categorie AND e.actif = TRUE)
                FROM categorie c
                """ + whereOrderBy);
        return mapCategories(q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private List<CategorieResponse> mapCategories(List<Object[]> rows) {
        return rows.stream().map(r -> {
            CategorieResponse c = new CategorieResponse();
            c.setId(toLong(r[0]));
            c.setIdParent(toLong(r[1]));
            c.setNom((String) r[2]);
            c.setDescription((String) r[3]);
            c.setIcone((String) r[4]);
            c.setCouleur((String) r[5]);
            c.setADesEnfants((Boolean) r[6]);
            return c;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TypeOffreResponse> typesOffre(Long idCategorie) {
        Query q = em.createNativeQuery("""
                SELECT id_type_offre, libelle, description, CAST(principale AS TEXT)
                FROM type_offre
                WHERE id_categorie = :idCategorie AND actif = TRUE
                ORDER BY libelle
                """);
        q.setParameter("idCategorie", idCategorie);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> {
            TypeOffreResponse t = new TypeOffreResponse();
            t.setId(toLong(r[0]));
            t.setLibelle((String) r[1]);
            t.setDescription((String) r[2]);
            t.setPrincipale((String) r[3]);
            return t;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public List<AttributResponse> attributs(Long idCategorie) {
        Query q = em.createNativeQuery("""
                SELECT id_attribut, nom, code, CAST(type_champ AS TEXT), obligatoire, unite
                FROM attribut
                WHERE id_categorie = :idCategorie AND actif = TRUE
                ORDER BY ordre_affichage, nom
                """);
        q.setParameter("idCategorie", idCategorie);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> {
            AttributResponse a = new AttributResponse();
            a.setId(toLong(r[0]));
            a.setNom((String) r[1]);
            a.setCode((String) r[2]);
            a.setTypeChamp((String) r[3]);
            a.setObligatoire((Boolean) r[4]);
            a.setUnite((String) r[5]);
            a.setValeurs(valeurs(a.getId()));
            return a;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<ValeurAttributResponse> valeurs(Long idAttribut) {
        Query q = em.createNativeQuery("""
                SELECT id_valeur, valeur
                FROM valeur_attribut_possible
                WHERE id_attribut = :idAttribut AND actif = TRUE
                ORDER BY ordre_affichage
                """);
        q.setParameter("idAttribut", idAttribut);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> {
            ValeurAttributResponse v = new ValeurAttributResponse();
            v.setId(toLong(r[0]));
            v.setValeur((String) r[1]);
            return v;
        }).toList();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
