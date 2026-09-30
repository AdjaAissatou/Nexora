package sn.ucad.nexora.administration.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.ActionJournalResponse;

/**
 * Lectures en SQL natif sur la base partagée : administration-service n'a pas de table propre en
 * écriture, il agrège ce que les autres services possèdent (même convention que les
 * *LookupRepository des autres services).
 */
@Component
public class AdministrationLectureRepository {

    private final EntityManager em;

    public AdministrationLectureRepository(EntityManager em) {
        this.em = em;
    }

    public long compter(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }

    /** Actions du journal, les plus récentes d'abord, avec filtres optionnels. */
    public List<ActionJournalResponse> journal(String module, String recherche, int limite, int decalage) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT j.id_journal_action, j.date_action,
                       TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')), u.email,
                       j.module, j.action, j.entite, j.id_entite, j.description, j.adresse_ip
                FROM journal_action j
                LEFT JOIN utilisateurs u ON u.id_utilisateur = j.id_utilisateur
                WHERE (CAST(:module AS TEXT) IS NULL OR j.module = CAST(:module AS TEXT))
                  AND (CAST(:recherche AS TEXT) IS NULL
                       OR j.description ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR j.action ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR u.email ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR (COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')) ILIKE '%' || CAST(:recherche AS TEXT) || '%')
                ORDER BY j.date_action DESC, j.id_journal_action DESC
                LIMIT :limite OFFSET :decalage
                """)
                .setParameter("module", module)
                .setParameter("recherche", recherche)
                .setParameter("limite", limite)
                .setParameter("decalage", decalage)
                .getResultList();
        return lignes.stream().map(l -> new ActionJournalResponse(
                ((Number) l[0]).longValue(),
                date(l[1]),
                vide((String) l[2]) ? null : (String) l[2], (String) l[3],
                (String) l[4], (String) l[5], (String) l[6],
                l[7] == null ? null : ((Number) l[7]).longValue(),
                (String) l[8], (String) l[9])).toList();
    }

    @SuppressWarnings("unchecked")
    public List<String> modulesJournal() {
        return em.createNativeQuery("SELECT DISTINCT module FROM journal_action ORDER BY module").getResultList();
    }

    /** Selon le pilote et Hibernate, une colonne TIMESTAMP native arrive en LocalDateTime ou en Timestamp. */
    private static LocalDateTime date(Object valeur) {
        if (valeur instanceof LocalDateTime d) return d;
        if (valeur instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
