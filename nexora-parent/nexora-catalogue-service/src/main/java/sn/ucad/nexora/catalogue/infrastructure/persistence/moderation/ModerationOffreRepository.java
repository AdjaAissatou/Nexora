package sn.ucad.nexora.catalogue.infrastructure.persistence.moderation;

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
import sn.ucad.nexora.catalogue.application.dto.response.moderation.ModerationOffreDtos.OffreModeree;
import sn.ucad.nexora.catalogue.application.dto.response.moderation.ModerationOffreDtos.Visibilite;

/** Lectures et écritures de la modération des offres, en SQL natif sur la base partagée. */
@Repository
public class ModerationOffreRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String SELECT = """
            SELECT o.id_offre, o.titre, o.prix, CAST(o.statut AS TEXT), c.nom,
                   ep.id_espace, ep.nom, CAST(ep.statut AS TEXT), u.id_utilisateur,
                   TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')),
                   o.date_creation, o.motif_moderation, o.date_moderation,
                   TRIM(COALESCE(m.prenom, '') || ' ' || COALESCE(m.nom, ''))
            FROM offre o
            JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
            JOIN utilisateurs u ON u.id_utilisateur = ep.id_utilisateur
            LEFT JOIN categorie c ON c.id_categorie = o.id_categorie
            LEFT JOIN utilisateurs m ON m.id_utilisateur = o.id_moderateur
            """;

    private static final String FILTRES = """
            WHERE CAST(o.statut AS TEXT) <> 'SUPPRIME'
              AND (CAST(:recherche AS TEXT) IS NULL OR o.titre ILIKE :motif OR ep.nom ILIKE :motif)
              AND (CAST(:statut AS TEXT) IS NULL OR CAST(o.statut AS TEXT) = :statut)
              AND (CAST(:espace AS TEXT) IS NULL OR o.id_espace = CAST(CAST(:espace AS TEXT) AS BIGINT))
            """;

    public List<OffreModeree> rechercher(String recherche, String statut, Long espaceId, int limite, int decalage) {
        Query q = em.createNativeQuery(SELECT + FILTRES + " ORDER BY o.date_creation DESC, o.id_offre DESC LIMIT :limite OFFSET :decalage");
        filtrer(q, recherche, statut, espaceId);
        q.setParameter("limite", limite).setParameter("decalage", decalage);
        return lignes(q);
    }

    public long compter(String recherche, String statut, Long espaceId) {
        Query q = em.createNativeQuery("""
                SELECT COUNT(*) FROM offre o JOIN espace_professionnel ep ON ep.id_espace = o.id_espace
                """ + FILTRES);
        filtrer(q, recherche, statut, espaceId);
        return ((Number) q.getSingleResult()).longValue();
    }

    public Optional<OffreModeree> offre(Long id) {
        return lignes(em.createNativeQuery(SELECT + " WHERE o.id_offre = :id").setParameter("id", id)).stream().findFirst();
    }

    public Optional<Visibilite> visibilite(Long offreId) {
        @SuppressWarnings("unchecked")
        List<Object[]> r = em.createNativeQuery("""
                SELECT CAST(o.statut AS TEXT), CAST(ep.statut AS TEXT), ep.id_utilisateur
                FROM offre o JOIN espace_professionnel ep ON ep.id_espace = o.id_espace WHERE o.id_offre = :id
                """).setParameter("id", offreId).getResultList();
        return r.stream().findFirst().map(l -> new Visibilite((String) l[0], (String) l[1], ((Number) l[2]).longValue()));
    }

    public Optional<Long> proprietaireEspace(Long espaceId) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT id_utilisateur FROM espace_professionnel WHERE id_espace = :id")
                .setParameter("id", espaceId).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    public void definirStatut(Long offreId, String statut, String motif, Long moderateurId) {
        em.createNativeQuery("""
                UPDATE offre
                SET statut = CAST(:statut AS statut_offre), motif_moderation = :motif,
                    date_moderation = NOW(), id_moderateur = :moderateur, date_modification = NOW()
                WHERE id_offre = :id
                """)
                .setParameter("statut", statut).setParameter("motif", motif)
                .setParameter("moderateur", moderateurId).setParameter("id", offreId)
                .executeUpdate();
    }

    public Optional<Long> utilisateurId(UUID accountId) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT id_utilisateur FROM utilisateurs WHERE account_id = :c")
                .setParameter("c", accountId).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    private static void filtrer(Query q, String recherche, String statut, Long espaceId) {
        q.setParameter("recherche", recherche)
                .setParameter("motif", recherche == null ? null : "%" + recherche + "%")
                .setParameter("statut", statut)
                .setParameter("espace", espaceId == null ? null : espaceId.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<OffreModeree> lignes(Query q) {
        List<Object[]> r = q.getResultList();
        return r.stream().map(l -> new OffreModeree(((Number) l[0]).longValue(), (String) l[1], (BigDecimal) l[2],
                (String) l[3], (String) l[4], ((Number) l[5]).longValue(), (String) l[6], (String) l[7],
                ((Number) l[8]).longValue(), (String) l[9], date(l[10]), (String) l[11], date(l[12]),
                l[13] == null || ((String) l[13]).isBlank() ? null : (String) l[13])).toList();
    }

    private static LocalDateTime date(Object o) {
        if (o instanceof LocalDateTime d) return d;
        if (o instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }
}
