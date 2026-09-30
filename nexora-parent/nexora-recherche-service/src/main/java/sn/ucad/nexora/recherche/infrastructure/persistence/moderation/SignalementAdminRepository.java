package sn.ucad.nexora.recherche.infrastructure.persistence.moderation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.SignalementResume;

/** Signalements en SQL natif : dépôt, doublons, file de traitement et décisions (§9.10). */
@Repository
public class SignalementAdminRepository {

    @PersistenceContext
    private EntityManager em;

    /** Type d'un signalement, déduit de la colonne renseignée. */
    private static final String TYPE = """
            CASE WHEN s.id_avis IS NOT NULL THEN 'AVIS' WHEN s.id_offre IS NOT NULL THEN 'OFFRE' ELSE 'ESPACE' END""";

    private static final String SELECT = """
            SELECT s.id_signalement,\s""" + TYPE + """
                   , COALESCE(s.id_avis, s.id_offre, s.id_espace),
                   CASE WHEN s.id_avis IS NOT NULL THEN '« ' || LEFT(COALESCE(a.commentaire, ''), 80) || ' »'
                        WHEN s.id_offre IS NOT NULL THEN o.titre ELSE e.nom END,
                   CASE WHEN s.id_avis IS NOT NULL THEN CASE WHEN a.masque THEN 'MASQUE' ELSE 'VISIBLE' END
                        WHEN s.id_offre IS NOT NULL THEN CAST(o.statut AS TEXT) ELSE CAST(e.statut AS TEXT) END,
                   COALESCE(e.id_espace, o.id_espace, a.id_espace, ao.id_espace),
                   s.motif, s.description, u.id_utilisateur,
                   TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')),
                   s.date_creation, CAST(s.statut AS TEXT),
                   TRIM(COALESCE(t.prenom, '') || ' ' || COALESCE(t.nom, '')),
                   s.date_traitement, s.commentaire_admin,
                   (SELECT COUNT(*) FROM signalement s2
                    WHERE s2.id_avis IS NOT DISTINCT FROM s.id_avis AND s2.id_offre IS NOT DISTINCT FROM s.id_offre
                      AND s2.id_espace IS NOT DISTINCT FROM s.id_espace)
            FROM signalement s
            JOIN utilisateurs u ON u.id_utilisateur = s.id_utilisateur
            LEFT JOIN espace_professionnel e ON e.id_espace = s.id_espace
            LEFT JOIN offre o ON o.id_offre = s.id_offre
            LEFT JOIN avis a ON a.id_avis = s.id_avis
            LEFT JOIN offre ao ON ao.id_offre = a.id_offre
            LEFT JOIN utilisateurs t ON t.id_utilisateur = s.traite_par
            """;

    private static final String FILTRES = """
            WHERE (CAST(:statut AS TEXT) IS NULL OR CAST(s.statut AS TEXT) = :statut)
              AND (CAST(:type AS TEXT) IS NULL OR\s""" + TYPE + """
             = :type)
            """;

    public void creer(Long utilisateurId, Long espaceId, Long offreId, Long avisId, String motif, String description) {
        em.createNativeQuery("""
                INSERT INTO signalement (id_utilisateur, id_espace, id_offre, id_avis, motif, description, statut, date_creation)
                VALUES (:u, :e, :o, :a, :motif, :description, 'EN_ATTENTE', NOW())
                """).setParameter("u", utilisateurId).setParameter("e", espaceId).setParameter("o", offreId)
                .setParameter("a", avisId).setParameter("motif", motif).setParameter("description", description)
                .executeUpdate();
    }

    /** Un signalement encore ouvert du même utilisateur sur la même cible. */
    public boolean dejaOuvert(Long utilisateurId, Long espaceId, Long offreId, Long avisId) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM signalement
                WHERE id_utilisateur = :u AND CAST(statut AS TEXT) IN ('EN_ATTENTE', 'EN_COURS')
                  AND id_espace IS NOT DISTINCT FROM CAST(CAST(:e AS TEXT) AS BIGINT)
                  AND id_offre IS NOT DISTINCT FROM CAST(CAST(:o AS TEXT) AS BIGINT)
                  AND id_avis IS NOT DISTINCT FROM CAST(CAST(:a AS TEXT) AS BIGINT)
                """).setParameter("u", utilisateurId).setParameter("e", texte(espaceId)).setParameter("o", texte(offreId))
                .setParameter("a", texte(avisId)).getSingleResult()).longValue() > 0;
    }

    public List<SignalementResume> rechercher(String statut, String type, int limite, int decalage) {
        Query q = em.createNativeQuery(SELECT + FILTRES + " ORDER BY s.date_creation DESC, s.id_signalement DESC LIMIT :limite OFFSET :decalage");
        q.setParameter("statut", statut).setParameter("type", type).setParameter("limite", limite).setParameter("decalage", decalage);
        return lignes(q);
    }

    public long compter(String statut, String type) {
        Query q = em.createNativeQuery("SELECT COUNT(*) FROM signalement s " + FILTRES);
        q.setParameter("statut", statut).setParameter("type", type);
        return ((Number) q.getSingleResult()).longValue();
    }

    public Optional<SignalementResume> signalement(Long id) {
        return lignes(em.createNativeQuery(SELECT + " WHERE s.id_signalement = :id").setParameter("id", id)).stream().findFirst();
    }

    public void definirStatut(Long id, String statut, Long moderateurId, String commentaire, boolean clos) {
        em.createNativeQuery("""
                UPDATE signalement SET statut = CAST(:statut AS statut_signalement), traite_par = :m,
                    commentaire_admin = COALESCE(CAST(:commentaire AS TEXT), commentaire_admin),
                    date_traitement = CASE WHEN :clos THEN NOW() ELSE date_traitement END
                WHERE id_signalement = :id
                """).setParameter("statut", statut).setParameter("m", moderateurId).setParameter("commentaire", commentaire)
                .setParameter("clos", clos).setParameter("id", id).executeUpdate();
    }

    private static String texte(Long v) {
        return v == null ? null : v.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<SignalementResume> lignes(Query q) {
        List<Object[]> r = q.getResultList();
        return r.stream().map(l -> new SignalementResume(((Number) l[0]).longValue(), (String) l[1],
                ((Number) l[2]).longValue(), (String) l[3], (String) l[4], l[5] == null ? null : ((Number) l[5]).longValue(),
                (String) l[6], (String) l[7], ((Number) l[8]).longValue(), (String) l[9], AvisModerationRepository.date(l[10]),
                (String) l[11], l[12] == null || ((String) l[12]).isBlank() ? null : (String) l[12],
                AvisModerationRepository.date(l[13]), (String) l[14], ((Number) l[15]).longValue())).toList();
    }
}
