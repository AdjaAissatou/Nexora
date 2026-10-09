package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Événements datés d'un espace (docs/architecture-acteurs.md §26) : vues des fiches et clics
 * ({@code evenement_statistique}), vues Découvrir, j'aime, appels et partages de Découvrir
 * ({@code decouverte_signal}), favoris ({@code favori}), réunis sous des types communs.
 */
@Repository
public class StatistiquesRepository {

    @PersistenceContext
    private EntityManager em;

    /** Les événements de l'espace depuis {@code debut} : (date, type, offre). */
    private static final String EVENEMENTS = """
            WITH ev AS (
                SELECT es.date_evenement AS d,
                       CASE WHEN es.type_evenement IN ('VUE_OFFRE', 'VUE_ESPACE') THEN 'VUE' ELSE es.type_evenement END AS t,
                       es.id_offre AS o
                FROM evenement_statistique es
                WHERE es.id_espace = :e AND es.date_evenement >= :debut
                UNION ALL
                SELECT s.date_signal,
                       CASE s.type_signal WHEN 'VUE' THEN 'VUE_DECOUVRIR' WHEN 'VUE_LONGUE' THEN 'VUE_DECOUVRIR'
                                          WHEN 'J_AIME' THEN 'JAIME' WHEN 'CONTACT' THEN 'APPEL' ELSE s.type_signal END,
                       s.id_offre
                FROM decouverte_signal s LEFT JOIN offre so ON so.id_offre = s.id_offre
                WHERE (so.id_espace = :e OR (s.id_offre IS NULL AND s.id_espace = :e))
                  AND s.type_signal IN ('VUE', 'VUE_LONGUE', 'J_AIME', 'CONTACT', 'PARTAGE') AND s.date_signal >= :debut
                UNION ALL
                SELECT f.date_creation, 'FAVORI', f.id_offre
                FROM favori f LEFT JOIN offre fo ON fo.id_offre = f.id_offre
                WHERE (fo.id_espace = :e OR (f.id_offre IS NULL AND f.id_espace = :e)) AND f.date_creation >= :debut
            )
            """;

    /** Par semaine (lundi) et type : (semaine, type, nombre). */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> parSemaine(Long espace, LocalDate debut) {
        return em.createNativeQuery(EVENEMENTS + """
                SELECT CAST(date_trunc('week', d) AS DATE), t, COUNT(*) FROM ev GROUP BY 1, 2""")
                .setParameter("e", espace).setParameter("debut", Date.valueOf(debut)).getResultList();
    }

    /** Totaux par type entre {@code debut} (inclus) et {@code fin} (exclue) : (type, nombre). */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> totaux(Long espace, LocalDateTime debut, LocalDateTime fin) {
        return em.createNativeQuery(EVENEMENTS + """
                SELECT t, COUNT(*) FROM ev WHERE d < :fin GROUP BY 1""")
                .setParameter("e", espace).setParameter("debut", Timestamp.valueOf(debut))
                .setParameter("fin", Timestamp.valueOf(fin)).getResultList();
    }

    /** Par offre et type depuis {@code debut} : (offre, type, nombre). */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> parOffre(Long espace, LocalDate debut) {
        return em.createNativeQuery(EVENEMENTS + """
                SELECT o, t, COUNT(*) FROM ev WHERE o IS NOT NULL GROUP BY 1, 2""")
                .setParameter("e", espace).setParameter("debut", Date.valueOf(debut)).getResultList();
    }

    /** Offres de l'espace (hors supprimées) : id, titre, image principale, badge populaire. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> offres(Long espace) {
        return em.createNativeQuery("""
                SELECT o.id_offre, o.titre,
                       (SELECT i.url FROM image i WHERE i.id_offre = o.id_offre ORDER BY i.principale DESC, i.ordre_affichage LIMIT 1),
                       """ + OffreRepositoryAdapter.POPULAIRE + """
                FROM offre o WHERE o.id_espace = :e ORDER BY o.id_offre""").setParameter("e", espace).getResultList();
    }

    /** Nom de l'espace si {@code compte} en est le propriétaire. */
    @Transactional(readOnly = true)
    public Optional<String> espaceDuCompte(Long espace, UUID compte) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("""
                SELECT e.nom FROM espace_professionnel e JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur
                WHERE e.id_espace = :e AND u.account_id = :c""").setParameter("e", espace).setParameter("c", compte).getResultList();
        return r.stream().findFirst().map(Object::toString);
    }

    /** Espace de l'offre, ou l'espace donné s'il existe. */
    @Transactional(readOnly = true)
    public Optional<Long> espaceDe(Long offre, Long espace) {
        @SuppressWarnings("unchecked")
        List<Object> r = offre != null
                ? em.createNativeQuery("SELECT id_espace FROM offre WHERE id_offre = :o").setParameter("o", offre).getResultList()
                : em.createNativeQuery("SELECT id_espace FROM espace_professionnel WHERE id_espace = :e").setParameter("e", espace).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    /** Enregistre un clic, sauf s'il répète le même clic du même visiteur dans les 30 secondes. */
    @Transactional
    public boolean enregistrer(Long espace, Long offre, String type, String visiteur) {
        return em.createNativeQuery("""
                INSERT INTO evenement_statistique (id_espace, id_offre, type_evenement, visiteur)
                SELECT :e, CAST(CAST(:o AS TEXT) AS BIGINT), :t, :v
                WHERE NOT EXISTS (SELECT 1 FROM evenement_statistique
                                  WHERE visiteur = :v AND type_evenement = :t AND id_espace = :e
                                    AND id_offre IS NOT DISTINCT FROM CAST(CAST(:o AS TEXT) AS BIGINT) AND date_evenement > NOW() - INTERVAL '30 seconds')""")
                .setParameter("e", espace).setParameter("o", offre == null ? null : offre.toString()).setParameter("t", type).setParameter("v", visiteur)
                .executeUpdate() > 0;
    }
}
