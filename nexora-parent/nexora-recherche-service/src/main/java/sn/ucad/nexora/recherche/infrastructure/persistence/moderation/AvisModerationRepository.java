package sn.ucad.nexora.recherche.infrastructure.persistence.moderation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import sn.ucad.nexora.recherche.application.dto.response.AvisDtos;
import sn.ucad.nexora.recherche.application.dto.response.AvisResponse;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.AvisModere;

/**
 * Avis en SQL natif sur la base partagée : lecture publique (avis visibles seulement), cibles d'un
 * avis ou d'un signalement, modération, et projection de la note sur l'espace.
 */
@Repository
public class AvisModerationRepository {

    @PersistenceContext
    private EntityManager em;

    /** Espace concerné par un avis : le sien, ou celui de son offre. */
    private static final String ESPACE_DE_L_AVIS = "COALESCE(a.id_espace, (SELECT o.id_espace FROM offre o WHERE o.id_offre = a.id_offre))";

    // ------------------------------------------------------------------ lecture publique

    public List<AvisResponse> visiblesParEspace(Long espaceId) {
        return publics("a.id_espace = :id", espaceId);
    }

    public List<AvisResponse> visiblesParOffre(Long offreId) {
        return publics("a.id_offre = :id", offreId);
    }

    public Optional<AvisResponse> visible(Long avisId) {
        return publics("a.id_avis = :id", avisId).stream().findFirst();
    }

    @SuppressWarnings("unchecked")
    private List<AvisResponse> publics(String condition, Long id) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT a.id_avis, a.id_utilisateur, a.id_offre, a.id_espace, a.note, a.commentaire,
                       a.reponse_fournisseur, a.date_creation, a.date_reponse,
                       TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(LEFT(u.nom, 1) || '.', '')), a.date_modification
                FROM avis a JOIN utilisateurs u ON u.id_utilisateur = a.id_utilisateur
                WHERE NOT a.masque AND\s""" + condition + " ORDER BY a.date_creation DESC, a.id_avis DESC")
                .setParameter("id", id).getResultList();
        return r.stream().map(l -> {
            AvisResponse a = new AvisResponse();
            a.setId(((Number) l[0]).longValue());
            a.setUtilisateurId(((Number) l[1]).longValue());
            a.setOffreId(l[2] == null ? null : ((Number) l[2]).longValue());
            a.setEspaceId(l[3] == null ? null : ((Number) l[3]).longValue());
            a.setNote(((Number) l[4]).intValue());
            a.setCommentaire((String) l[5]);
            a.setReponseFournisseur((String) l[6]);
            a.setDateCreation(date(l[7]));
            a.setDateReponse(date(l[8]));
            a.setAuteur((String) l[9]);
            a.setDateModification(date(l[10]));
            return a;
        }).toList();
    }

    /** L'avis d'un utilisateur sur un espace, même masqué : il le voit, avec le motif s'il a été masqué. */
    public record MonAvis(Long id, int note, String commentaire, boolean masque, String motifModeration) {}

    public Optional<MonAvis> monAvisSurEspace(Long utilisateurId, Long espaceId) {
        @SuppressWarnings("unchecked")
        List<Object[]> r = em.createNativeQuery("""
                SELECT id_avis, note, commentaire, masque, motif_moderation FROM avis
                WHERE id_utilisateur = :u AND id_espace = :e ORDER BY id_avis DESC LIMIT 1
                """).setParameter("u", utilisateurId).setParameter("e", espaceId).getResultList();
        return r.stream().findFirst().map(l -> new MonAvis(((Number) l[0]).longValue(), ((Number) l[1]).intValue(),
                (String) l[2], Boolean.TRUE.equals(l[3]), (String) l[4]));
    }

    public boolean aDejaNoteEspace(Long utilisateurId, Long espaceId) {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM avis WHERE id_utilisateur = :u AND id_espace = :e")
                .setParameter("u", utilisateurId).setParameter("e", espaceId).getSingleResult()).longValue() > 0;
    }

    // ------------------------------------------------------------------ mes avis, avis reçus (§22)

    /** Tous les avis d'un utilisateur, masqués compris, les plus récents d'abord. */
    @SuppressWarnings("unchecked")
    public List<AvisDtos.MonAvisDetail> mesAvis(Long utilisateurId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT a.id_avis, a.note, a.commentaire, a.date_creation, a.date_modification, a.masque, a.motif_moderation,
                       a.reponse_fournisseur, a.date_reponse, e.id_espace, e.nom, o.id_offre, o.titre,
                       COALESCE((SELECT i.url FROM image i WHERE i.id_offre = o.id_offre ORDER BY i.principale DESC, i.ordre_affichage LIMIT 1),
                                NULLIF(e.couverture, ''), NULLIF(e.logo, ''),
                                (SELECT i.url FROM image i JOIN offre o2 ON o2.id_offre = i.id_offre
                                 WHERE o2.id_espace = e.id_espace ORDER BY i.principale DESC, i.id_image LIMIT 1)),
                       CAST(e.statut AS TEXT) = 'ACTIF' AND (o.id_offre IS NULL OR CAST(o.statut AS TEXT) = 'PUBLIE')
                FROM avis a
                LEFT JOIN offre o ON o.id_offre = a.id_offre
                LEFT JOIN espace_professionnel e ON e.id_espace = COALESCE(a.id_espace, o.id_espace)
                WHERE a.id_utilisateur = :u
                ORDER BY COALESCE(a.date_modification, a.date_creation) DESC, a.id_avis DESC
                """).setParameter("u", utilisateurId).getResultList();
        return r.stream().map(l -> new AvisDtos.MonAvisDetail(((Number) l[0]).longValue(), ((Number) l[1]).intValue(),
                (String) l[2], date(l[3]), date(l[4]), Boolean.TRUE.equals(l[5]), (String) l[6], (String) l[7], date(l[8]),
                l[9] == null ? null : ((Number) l[9]).longValue(), (String) l[10],
                l[11] == null ? null : ((Number) l[11]).longValue(), (String) l[12], (String) l[13],
                Boolean.TRUE.equals(l[14]))).toList();
    }

    /** Avis visibles d'un espace (sur lui et sur ses offres), les plus récents d'abord, et nombre d'avis masqués. */
    @SuppressWarnings("unchecked")
    public List<AvisDtos.AvisRecu> avisRecus(Long espaceId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT a.id_avis, a.note, a.commentaire,
                       TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(LEFT(u.nom, 1) || '.', '')),
                       a.date_creation, a.date_modification, o.id_offre, o.titre, a.reponse_fournisseur, a.date_reponse
                FROM avis a
                JOIN utilisateurs u ON u.id_utilisateur = a.id_utilisateur
                LEFT JOIN offre o ON o.id_offre = a.id_offre
                WHERE NOT a.masque AND (a.id_espace = :e OR o.id_espace = :e)
                ORDER BY a.date_creation DESC, a.id_avis DESC
                """).setParameter("e", espaceId).getResultList();
        return r.stream().map(l -> new AvisDtos.AvisRecu(((Number) l[0]).longValue(), ((Number) l[1]).intValue(),
                (String) l[2], (String) l[3], date(l[4]), date(l[5]), l[6] == null ? null : ((Number) l[6]).longValue(),
                (String) l[7], (String) l[8], date(l[9]))).toList();
    }

    public int avisMasques(Long espaceId) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM avis a LEFT JOIN offre o ON o.id_offre = a.id_offre
                WHERE a.masque AND (a.id_espace = :e OR o.id_espace = :e)""")
                .setParameter("e", espaceId).getSingleResult()).intValue();
    }

    /** L'avis (auteur, masqué, réponse actuelle) et l'espace concerné, pour les règles de modification et de réponse. */
    public record EtatAvis(Long auteurId, boolean masque, String reponse, Long espaceId, String espaceNom,
                           Long proprietaireId, Long offreId) {}

    @SuppressWarnings("unchecked")
    public Optional<EtatAvis> etat(Long avisId) {
        List<Object[]> r = em.createNativeQuery("""
                SELECT a.id_utilisateur, a.masque, a.reponse_fournisseur, e.id_espace, e.nom, e.id_utilisateur, a.id_offre
                FROM avis a
                LEFT JOIN offre o ON o.id_offre = a.id_offre
                JOIN espace_professionnel e ON e.id_espace = COALESCE(a.id_espace, o.id_espace)
                WHERE a.id_avis = :id
                """).setParameter("id", avisId).getResultList();
        return r.stream().findFirst().map(l -> new EtatAvis(((Number) l[0]).longValue(), Boolean.TRUE.equals(l[1]),
                (String) l[2], ((Number) l[3]).longValue(), (String) l[4], ((Number) l[5]).longValue(),
                l[6] == null ? null : ((Number) l[6]).longValue()));
    }

    public void modifier(Long avisId, int note, String commentaire) {
        em.createNativeQuery("UPDATE avis SET note = :n, commentaire = :c, date_modification = NOW() WHERE id_avis = :id")
                .setParameter("n", note).setParameter("c", commentaire).setParameter("id", avisId).executeUpdate();
    }

    /** {@code texte} null : la réponse est retirée. */
    public void definirReponse(Long avisId, String texte) {
        em.createNativeQuery("""
                UPDATE avis SET reponse_fournisseur = CAST(:t AS TEXT),
                       date_reponse = CASE WHEN CAST(:t AS TEXT) IS NULL THEN NULL ELSE NOW() END
                WHERE id_avis = :id""").setParameter("t", texte).setParameter("id", avisId).executeUpdate();
    }

    // ------------------------------------------------------------------ cibles

    /** Cible visible et son propriétaire : statut de l'espace, propriétaire, espace. */
    public record Cible(String libelle, boolean visible, Long proprietaireId, Long espaceId) {}

    public Optional<Cible> cibleEspace(Long espaceId) {
        return cible("""
                SELECT e.nom, CAST(e.statut AS TEXT) = 'ACTIF', e.id_utilisateur, e.id_espace
                FROM espace_professionnel e WHERE e.id_espace = :id""", espaceId);
    }

    public Optional<Cible> cibleOffre(Long offreId) {
        return cible("""
                SELECT o.titre, CAST(o.statut AS TEXT) = 'PUBLIE' AND CAST(e.statut AS TEXT) = 'ACTIF', e.id_utilisateur, e.id_espace
                FROM offre o JOIN espace_professionnel e ON e.id_espace = o.id_espace WHERE o.id_offre = :id""", offreId);
    }

    /** Pour un avis, le « propriétaire » est son auteur. */
    public Optional<Cible> cibleAvis(Long avisId) {
        return cible("""
                SELECT LEFT(COALESCE(a.commentaire, ''), 80), NOT a.masque, a.id_utilisateur, """ + ESPACE_DE_L_AVIS + " " + """
                FROM avis a WHERE a.id_avis = :id""", avisId);
    }

    public Optional<Long> proprietaireEspace(Long espaceId) {
        return cibleEspace(espaceId).map(Cible::proprietaireId);
    }

    @SuppressWarnings("unchecked")
    private Optional<Cible> cible(String sql, Long id) {
        List<Object[]> r = em.createNativeQuery(sql).setParameter("id", id).getResultList();
        return r.stream().findFirst().map(l -> new Cible((String) l[0], Boolean.TRUE.equals(l[1]),
                ((Number) l[2]).longValue(), l[3] == null ? null : ((Number) l[3]).longValue()));
    }

    // ------------------------------------------------------------------ note de l'espace

    /**
     * La note d'un espace est une projection de ses avis visibles (sur l'espace et sur ses offres) :
     * recalculée à chaque avis publié, supprimé, masqué ou rétabli.
     */
    public void recalculerNote(Long espaceId) {
        if (espaceId == null) return;
        em.createNativeQuery("""
                UPDATE espace_professionnel e SET
                    nombre_avis = s.n,
                    note_moyenne = CASE WHEN s.n = 0 THEN 0 ELSE s.moyenne END
                FROM (SELECT COUNT(*) AS n, ROUND(COALESCE(AVG(a.note), 0), 1) AS moyenne
                      FROM avis a
                      WHERE NOT a.masque
                        AND (a.id_espace = :id OR a.id_offre IN (SELECT id_offre FROM offre WHERE id_espace = :id))) s
                WHERE e.id_espace = :id
                """).setParameter("id", espaceId).executeUpdate();
    }

    // ------------------------------------------------------------------ modération

    private static final String SELECT = """
            SELECT a.id_avis, a.note, a.commentaire, u.id_utilisateur,
                   TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')), u.email,
                   e.id_espace, e.nom, o.id_offre, o.titre, a.date_creation, a.masque, a.motif_moderation,
                   a.date_moderation, TRIM(COALESCE(m.prenom, '') || ' ' || COALESCE(m.nom, '')),
                   (SELECT COUNT(*) FROM signalement s WHERE s.id_avis = a.id_avis
                      AND CAST(s.statut AS TEXT) IN ('EN_ATTENTE', 'EN_COURS'))
            FROM avis a
            JOIN utilisateurs u ON u.id_utilisateur = a.id_utilisateur
            LEFT JOIN offre o ON o.id_offre = a.id_offre
            LEFT JOIN espace_professionnel e ON e.id_espace = COALESCE(a.id_espace, o.id_espace)
            LEFT JOIN utilisateurs m ON m.id_utilisateur = a.id_moderateur
            """;

    private static final String FILTRES = """
            WHERE (CAST(:recherche AS TEXT) IS NULL OR a.commentaire ILIKE :motif OR e.nom ILIKE :motif
                   OR (COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')) ILIKE :motif)
              AND (CAST(:etat AS TEXT) IS NULL OR a.masque = (CAST(:etat AS TEXT) = 'MASQUE'))
              AND (CAST(:espace AS TEXT) IS NULL OR e.id_espace = CAST(CAST(:espace AS TEXT) AS BIGINT))
            """;

    public List<AvisModere> rechercher(String recherche, String etat, Long espaceId, int limite, int decalage) {
        Query q = em.createNativeQuery(SELECT + FILTRES + " ORDER BY a.date_creation DESC, a.id_avis DESC LIMIT :limite OFFSET :decalage");
        filtrer(q, recherche, etat, espaceId);
        return lignes(q.setParameter("limite", limite).setParameter("decalage", decalage));
    }

    public long compter(String recherche, String etat, Long espaceId) {
        Query q = em.createNativeQuery("""
                SELECT COUNT(*) FROM avis a
                JOIN utilisateurs u ON u.id_utilisateur = a.id_utilisateur
                LEFT JOIN offre o ON o.id_offre = a.id_offre
                LEFT JOIN espace_professionnel e ON e.id_espace = COALESCE(a.id_espace, o.id_espace)
                """ + FILTRES);
        filtrer(q, recherche, etat, espaceId);
        return ((Number) q.getSingleResult()).longValue();
    }

    public Optional<AvisModere> avis(Long id) {
        return lignes(em.createNativeQuery(SELECT + " WHERE a.id_avis = :id").setParameter("id", id)).stream().findFirst();
    }

    public void definirMasque(Long avisId, boolean masque, String motif, Long moderateurId) {
        em.createNativeQuery("""
                UPDATE avis SET masque = :masque, motif_moderation = :motif, date_moderation = NOW(), id_moderateur = :moderateur
                WHERE id_avis = :id
                """).setParameter("masque", masque).setParameter("motif", motif)
                .setParameter("moderateur", moderateurId).setParameter("id", avisId).executeUpdate();
    }

    public Optional<Long> utilisateurId(UUID accountId) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT id_utilisateur FROM utilisateurs WHERE account_id = :c")
                .setParameter("c", accountId).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    private static void filtrer(Query q, String recherche, String etat, Long espaceId) {
        q.setParameter("recherche", recherche)
                .setParameter("motif", recherche == null ? null : "%" + recherche + "%")
                .setParameter("etat", etat)
                .setParameter("espace", espaceId == null ? null : espaceId.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<AvisModere> lignes(Query q) {
        List<Object[]> r = q.getResultList();
        return r.stream().map(l -> new AvisModere(((Number) l[0]).longValue(), ((Number) l[1]).intValue(), (String) l[2],
                ((Number) l[3]).longValue(), (String) l[4], (String) l[5],
                l[6] == null ? null : ((Number) l[6]).longValue(), (String) l[7],
                l[8] == null ? null : ((Number) l[8]).longValue(), (String) l[9], date(l[10]),
                Boolean.TRUE.equals(l[11]), (String) l[12], date(l[13]),
                l[14] == null || ((String) l[14]).isBlank() ? null : (String) l[14], ((Number) l[15]).longValue())).toList();
    }

    static LocalDateTime date(Object o) {
        if (o instanceof LocalDateTime d) return d;
        if (o instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }
}
