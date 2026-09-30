package sn.ucad.nexora.espace.infrastructure.persistence.moderation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.ActionHistorique;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.EspaceModere;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.ModerationVisible;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.OffreDeLEspace;

/**
 * Lectures et écritures de la modération des espaces, en SQL natif sur la base partagée (le nombre
 * d'offres vient de la table {@code offre}, le propriétaire de {@code utilisateurs}).
 */
@Repository
public class ModerationEspaceRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String SELECT = """
            SELECT e.id_espace, e.nom, te.nom, a.commune, CAST(e.statut AS TEXT), e.verifie, e.certifie,
                   u.id_utilisateur, u.account_id, TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')), u.email,
                   (SELECT COUNT(*) FROM offre o WHERE o.id_espace = e.id_espace AND CAST(o.statut AS TEXT) = 'PUBLIE'),
                   (SELECT COUNT(*) FROM offre o WHERE o.id_espace = e.id_espace AND CAST(o.statut AS TEXT) = 'SUSPENDU'),
                   e.date_creation, e.motif_moderation, e.date_moderation,
                   TRIM(COALESCE(m.prenom, '') || ' ' || COALESCE(m.nom, ''))
            FROM espace_professionnel e
            JOIN type_espace te ON te.id_type_espace = e.id_type_espace
            JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur
            LEFT JOIN adresse a ON a.id_espace = e.id_espace AND a.principale = TRUE
            LEFT JOIN utilisateurs m ON m.id_utilisateur = e.id_moderateur
            """;

    private static final String FILTRES = """
            WHERE (CAST(:recherche AS TEXT) IS NULL OR e.nom ILIKE :motif OR u.email ILIKE :motif
                   OR (COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')) ILIKE :motif)
              AND (CAST(:statut AS TEXT) IS NULL OR CAST(e.statut AS TEXT) = :statut)
              AND (CAST(:verifie AS TEXT) IS NULL OR e.verifie = (CAST(:verifie AS TEXT) = 'true'))
            """;

    public List<EspaceModere> rechercher(String recherche, String statut, Boolean verifie, int limite, int decalage) {
        Query q = em.createNativeQuery(SELECT + FILTRES + " ORDER BY e.date_creation DESC, e.id_espace DESC LIMIT :limite OFFSET :decalage");
        filtrer(q, recherche, statut, verifie);
        q.setParameter("limite", limite).setParameter("decalage", decalage);
        return lignes(q);
    }

    public long compter(String recherche, String statut, Boolean verifie) {
        Query q = em.createNativeQuery("""
                SELECT COUNT(*) FROM espace_professionnel e
                JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur
                """ + FILTRES);
        filtrer(q, recherche, statut, verifie);
        return ((Number) q.getSingleResult()).longValue();
    }

    public Optional<EspaceModere> espace(Long id) {
        Query q = em.createNativeQuery(SELECT + " WHERE e.id_espace = :id").setParameter("id", id);
        return lignes(q).stream().findFirst();
    }

    /** Description, téléphone, e-mail et adresse lisible de l'espace. */
    public Object[] details(Long id) {
        return (Object[]) em.createNativeQuery("""
                SELECT e.description, e.telephone, e.email,
                       CONCAT_WS(', ', NULLIF(a.adresse_complete, ''), NULLIF(a.quartier, ''), NULLIF(a.commune, ''), NULLIF(a.region, ''))
                FROM espace_professionnel e
                LEFT JOIN adresse a ON a.id_espace = e.id_espace AND a.principale = TRUE
                WHERE e.id_espace = :id
                """).setParameter("id", id).getSingleResult();
    }

    public List<OffreDeLEspace> offres(Long espaceId) {
        @SuppressWarnings("unchecked")
        List<Object[]> r = em.createNativeQuery("""
                SELECT id_offre, titre, prix, CAST(statut AS TEXT), motif_moderation, date_moderation
                FROM offre WHERE id_espace = :id AND CAST(statut AS TEXT) <> 'SUPPRIME'
                ORDER BY date_creation DESC, id_offre DESC
                """).setParameter("id", espaceId).getResultList();
        return r.stream().map(l -> new OffreDeLEspace(((Number) l[0]).longValue(), (String) l[1], (BigDecimal) l[2],
                (String) l[3], (String) l[4], date(l[5]))).toList();
    }

    public List<ActionHistorique> historique(Long espaceId) {
        @SuppressWarnings("unchecked")
        List<Object[]> r = em.createNativeQuery("""
                SELECT j.date_action, TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')), j.action, j.description
                FROM journal_action j LEFT JOIN utilisateurs u ON u.id_utilisateur = j.id_utilisateur
                WHERE (j.entite = 'espace' AND j.id_entite = :id)
                   OR (j.entite = 'offre' AND j.id_entite IN (SELECT id_offre FROM offre WHERE id_espace = :id))
                ORDER BY j.date_action DESC, j.id_journal_action DESC LIMIT 50
                """).setParameter("id", espaceId).getResultList();
        return r.stream().map(l -> new ActionHistorique(date(l[0]), (String) l[1], (String) l[2], (String) l[3])).toList();
    }

    public Optional<ModerationVisible> moderationVisible(Long espaceId) {
        @SuppressWarnings("unchecked")
        List<Object[]> r = em.createNativeQuery("""
                SELECT CAST(statut AS TEXT), motif_moderation, date_moderation FROM espace_professionnel WHERE id_espace = :id
                """).setParameter("id", espaceId).getResultList();
        return r.stream().findFirst().map(l -> new ModerationVisible((String) l[0], (String) l[1], date(l[2])));
    }

    /** Statut modéré : motif et modérateur gardés pour une suspension, effacés pour une réactivation. */
    public void definirStatut(Long espaceId, String statut, String motif, Long moderateurId) {
        em.createNativeQuery("""
                UPDATE espace_professionnel
                SET statut = CAST(:statut AS statut_espace), motif_moderation = :motif,
                    date_moderation = NOW(), id_moderateur = :moderateur, date_modification = NOW()
                WHERE id_espace = :id
                """)
                .setParameter("statut", statut).setParameter("motif", motif)
                .setParameter("moderateur", moderateurId).setParameter("id", espaceId)
                .executeUpdate();
    }

    public Optional<Long> utilisateurId(UUID accountId) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT id_utilisateur FROM utilisateurs WHERE account_id = :c")
                .setParameter("c", accountId).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    private static void filtrer(Query q, String recherche, String statut, Boolean verifie) {
        q.setParameter("recherche", recherche)
                .setParameter("motif", recherche == null ? null : "%" + recherche + "%")
                .setParameter("statut", statut)
                .setParameter("verifie", verifie == null ? null : verifie.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<EspaceModere> lignes(Query q) {
        List<Object[]> r = q.getResultList();
        return r.stream().map(l -> new EspaceModere(((Number) l[0]).longValue(), (String) l[1], (String) l[2], (String) l[3],
                (String) l[4], Boolean.TRUE.equals(l[5]), Boolean.TRUE.equals(l[6]), ((Number) l[7]).longValue(),
                l[8] == null ? null : (UUID) l[8], (String) l[9], (String) l[10], ((Number) l[11]).longValue(),
                ((Number) l[12]).longValue(), date(l[13]), (String) l[14], date(l[15]),
                l[16] == null || ((String) l[16]).isBlank() ? null : (String) l[16])).toList();
    }

    /** Hibernate 7 rend les TIMESTAMP natifs en LocalDateTime ou en Timestamp selon le pilote. */
    private static LocalDateTime date(Object o) {
        if (o instanceof LocalDateTime d) return d;
        if (o instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }
}
