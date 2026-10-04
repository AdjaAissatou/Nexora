package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.catalogue.application.dto.response.LieuPublicResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lecture des lieux publics (marchés, lieux de culte, hôpitaux, gares...) utilisés comme
 * repères de recherche/carte. Requête native, cohérent avec CategorieQueryRepository.
 */
@Component
public class LieuPublicQueryRepository {

    private final EntityManager em;

    public LieuPublicQueryRepository(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    public List<LieuPublicResponse> rechercher(String q, String commune, String typeLieu, int limite) {
        Query query = em.createNativeQuery("""
                SELECT id_lieu, nom, type_lieu, region, departement, commune, adresse_complete,
                       latitude, longitude, description
                FROM lieu_public
                WHERE actif = TRUE
                  -- Sans accents : « tilene » trouve « Marché Tilène »
                  AND (CAST(:q AS TEXT) IS NULL OR translate(lower(nom), :accents, :sans)
                       LIKE '%' || translate(lower(CAST(:q AS TEXT)), :accents, :sans) || '%')
                  AND (CAST(:commune AS TEXT) IS NULL OR commune ILIKE CAST(:commune AS TEXT))
                  AND (CAST(:typeLieu AS TEXT) IS NULL OR type_lieu = CAST(:typeLieu AS TEXT))
                ORDER BY nom
                LIMIT :limite
                """);
        query.setParameter("q", vide(q));
        query.setParameter("commune", vide(commune));
        query.setParameter("typeLieu", vide(typeLieu));
        query.setParameter("limite", limite);
        query.setParameter("accents", ACCENTS);
        query.setParameter("sans", SANS_ACCENTS);
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(this::mapper).toList();
    }

    @SuppressWarnings("unchecked")
    public java.util.Optional<LieuPublicResponse> parId(Long id) {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT id_lieu, nom, type_lieu, region, departement, commune, adresse_complete,
                       latitude, longitude, description
                FROM lieu_public WHERE id_lieu = :id AND actif = TRUE
                """).setParameter("id", id).getResultList();
        return rows.stream().findFirst().map(this::mapper);
    }

    private static final String ACCENTS = "éèêëàâäîïôöûüùçœ";
    private static final String SANS_ACCENTS = "eeeeaaaiioouuuco";

    private String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private LieuPublicResponse mapper(Object[] r) {
        LieuPublicResponse l = new LieuPublicResponse();
        l.setId(((Number) r[0]).longValue());
        l.setNom((String) r[1]);
        l.setTypeLieu((String) r[2]);
        l.setRegion((String) r[3]);
        l.setDepartement((String) r[4]);
        l.setCommune((String) r[5]);
        l.setAdresseComplete((String) r[6]);
        l.setLatitude(r[7] == null ? null : new BigDecimal(r[7].toString()));
        l.setLongitude(r[8] == null ? null : new BigDecimal(r[8].toString()));
        l.setDescription((String) r[9]);
        return l;
    }
}
