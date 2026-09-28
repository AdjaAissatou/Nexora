package sn.ucad.nexora.espace.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.espace.application.dto.response.CommuneResponse;
import sn.ucad.nexora.espace.application.dto.response.DepartementResponse;
import sn.ucad.nexora.espace.application.dto.response.RegionResponse;

import java.util.List;

/**
 * Lecture de la référence géographique du Sénégal (région > département > commune) — alimente
 * la sélection en cascade de la localisation lors de la création/modification d'un espace, et
 * sert à vérifier qu'une commune choisie appartient bien au département et à la région soumis.
 * Requêtes natives, cohérent avec CategorieQueryRepository côté catalogue-service.
 */
@Component
public class GeoQueryRepository {

    private final EntityManager em;

    public GeoQueryRepository(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    public List<RegionResponse> regions() {
        Query q = em.createNativeQuery("SELECT id_region, nom FROM region ORDER BY ordre_affichage, nom");
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> {
            RegionResponse v = new RegionResponse();
            v.setId(toLong(r[0]));
            v.setNom((String) r[1]);
            return v;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public List<DepartementResponse> departements(Long idRegion) {
        Query q = em.createNativeQuery("SELECT id_departement, nom FROM departement WHERE id_region = :idRegion ORDER BY nom");
        q.setParameter("idRegion", idRegion);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> {
            DepartementResponse v = new DepartementResponse();
            v.setId(toLong(r[0]));
            v.setNom((String) r[1]);
            return v;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    public List<CommuneResponse> communes(Long idDepartement) {
        Query q = em.createNativeQuery("SELECT id_commune, nom FROM commune WHERE id_departement = :idDepartement ORDER BY nom");
        q.setParameter("idDepartement", idDepartement);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> {
            CommuneResponse v = new CommuneResponse();
            v.setId(toLong(r[0]));
            v.setNom((String) r[1]);
            return v;
        }).toList();
    }

    /**
     * Résout et vérifie en un seul aller-retour la chaîne région/département/commune soumise
     * lors de la création/modification d'un espace : lève une exception si la commune choisie
     * n'appartient pas au département soumis, ou le département à la région soumise.
     */
    public GeoNoms verifierEtResoudre(Long idRegion, Long idDepartement, Long idCommune) {
        Query q = em.createNativeQuery("""
                SELECT r.nom, d.nom, c.nom, d.id_region, c.id_departement
                FROM commune c
                JOIN departement d ON d.id_departement = c.id_departement
                JOIN region r ON r.id_region = d.id_region
                WHERE c.id_commune = :idCommune
                """);
        q.setParameter("idCommune", idCommune);
        Object[] row;
        try {
            row = (Object[]) q.getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Commune introuvable");
        }
        if (!toLong(row[4]).equals(idDepartement)) {
            throw new IllegalArgumentException("Cette commune n'appartient pas au département choisi");
        }
        if (!toLong(row[3]).equals(idRegion)) {
            throw new IllegalArgumentException("Ce département n'appartient pas à la région choisie");
        }
        return new GeoNoms((String) row[0], (String) row[1], (String) row[2]);
    }

    public record GeoNoms(String region, String departement, String commune) {}

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
