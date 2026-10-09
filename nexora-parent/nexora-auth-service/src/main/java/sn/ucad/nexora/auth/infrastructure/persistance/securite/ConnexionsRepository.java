package sn.ucad.nexora.auth.infrastructure.persistance.securite;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.auth.application.dto.securite.SecuriteDtos.Connexion;
import sn.ucad.nexora.auth.application.dto.securite.SecuriteDtos.TentativeEchouee;
import sn.ucad.nexora.common.audit.ClientHttp;

/**
 * Connexions et sessions des comptes (table {@code connexion_compte}) et changements d'email en attente
 * (table {@code changement_email}), docs/architecture-acteurs.md §25.
 */
@Repository
public class ConnexionsRepository {

    @PersistenceContext
    private EntityManager em;

    /** Ouvre une session pour une connexion réussie ; renvoie son identifiant (claim « sid » des jetons). */
    @Transactional
    public String ouvrirSession(UUID compte, String ip, String agent) {
        UUID sid = UUID.randomUUID();
        em.createNativeQuery("""
                INSERT INTO connexion_compte (account_id, id_session, reussie, adresse_ip, user_agent)
                VALUES (:c, :s, TRUE, :ip, :ua)""")
                .setParameter("c", compte).setParameter("s", sid).setParameter("ip", ip).setParameter("ua", agent)
                .executeUpdate();
        return sid.toString();
    }

    /** Mot de passe erroné pour un compte existant : gardé pour l'alerte « tentatives échouées ». */
    @Transactional
    public void echec(UUID compte, String ip, String agent) {
        em.createNativeQuery("""
                INSERT INTO connexion_compte (account_id, reussie, adresse_ip, user_agent) VALUES (:c, FALSE, :ip, :ua)""")
                .setParameter("c", compte).setParameter("ip", ip).setParameter("ua", agent).executeUpdate();
    }

    /** Session encore ouverte ? (une session inconnue, d'avant cette fonctionnalité, n'est pas bloquée). */
    @Transactional
    public boolean sessionValide(String sid) {
        UUID id = uuid(sid);
        if (id == null) return true;
        @SuppressWarnings("unchecked")
        List<Object> fins = em.createNativeQuery("SELECT date_fin FROM connexion_compte WHERE id_session = :s")
                .setParameter("s", id).getResultList();
        if (fins.isEmpty()) return true;
        if (fins.get(0) != null) return false;
        em.createNativeQuery("UPDATE connexion_compte SET derniere_activite = NOW() WHERE id_session = :s")
                .setParameter("s", id).executeUpdate();
        return true;
    }

    /** Ferme une session de ce compte ; 1 si elle était ouverte. */
    @Transactional
    public int terminer(UUID compte, String sid, String motif) {
        UUID id = uuid(sid);
        if (id == null) return 0;
        return em.createNativeQuery("""
                UPDATE connexion_compte SET date_fin = NOW(), motif_fin = :m
                WHERE id_session = :s AND account_id = :c AND date_fin IS NULL""")
                .setParameter("m", motif).setParameter("s", id).setParameter("c", compte).executeUpdate();
    }

    /** Ferme toutes les sessions ouvertes du compte sauf {@code garder} (null : toutes). */
    @Transactional
    public int terminerAutres(UUID compte, String garder, String motif) {
        return em.createNativeQuery("""
                UPDATE connexion_compte SET date_fin = NOW(), motif_fin = :m
                WHERE account_id = :c AND reussie AND date_fin IS NULL
                  AND (CAST(:g AS UUID) IS NULL OR id_session <> CAST(:g AS UUID))""")
                .setParameter("m", motif).setParameter("c", compte).setParameter("g", garder).executeUpdate();
    }

    /** Sessions ouvertes (les plus récentes d'abord). Au-delà de 30 jours sans activité, une session est considérée terminée. */
    @Transactional(readOnly = true)
    public List<Connexion> sessions(UUID compte, String actuelle, boolean actives, int limite) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT CAST(id_session AS TEXT), date_connexion, derniere_activite, adresse_ip, user_agent, date_fin, motif_fin
                FROM connexion_compte
                WHERE account_id = :c AND reussie
                  AND (CASE WHEN :actives THEN date_fin IS NULL AND derniere_activite > NOW() - INTERVAL '30 days'
                            ELSE date_fin IS NOT NULL OR derniere_activite <= NOW() - INTERVAL '30 days' END)
                ORDER BY derniere_activite DESC LIMIT :l""")
                .setParameter("c", compte).setParameter("actives", actives).setParameter("l", limite).getResultList();
        return lignes.stream().map(l -> new Connexion((String) l[0], date(l[1]), date(l[2]), (String) l[3],
                ClientHttp.appareil((String) l[4]), l[5] == null && actives, l[0] != null && l[0].equals(actuelle),
                date(l[5]), (String) l[6])).toList();
    }

    /** Tentatives échouées des 30 derniers jours. */
    @Transactional(readOnly = true)
    public List<TentativeEchouee> echecs(UUID compte, int limite) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT date_connexion, adresse_ip, user_agent FROM connexion_compte
                WHERE account_id = :c AND NOT reussie AND date_connexion > NOW() - INTERVAL '30 days'
                ORDER BY date_connexion DESC LIMIT :l""").setParameter("c", compte).setParameter("l", limite).getResultList();
        return lignes.stream().map(l -> new TentativeEchouee(date(l[0]), (String) l[1], ClientHttp.appareil((String) l[2]))).toList();
    }

    // ------------------------------------------------------------------ changement d'email

    @Transactional
    public void demanderEmail(UUID compte, String email, String code, LocalDateTime expiration) {
        em.createNativeQuery("""
                UPDATE changement_email SET date_expiration = LEAST(date_expiration, NOW())
                WHERE account_id = :c AND date_confirmation IS NULL""").setParameter("c", compte).executeUpdate();
        em.createNativeQuery("""
                INSERT INTO changement_email (account_id, nouvel_email, code, date_expiration) VALUES (:c, :e, :code, :x)""")
                .setParameter("c", compte).setParameter("e", email).setParameter("code", code)
                .setParameter("x", Timestamp.valueOf(expiration)).executeUpdate();
    }

    /** Demande en cours (non confirmée, non expirée) : id, nouvel email, code, essais. */
    public record DemandeEmail(Long id, String email, String code, int essais) {}

    @Transactional(readOnly = true)
    public Optional<DemandeEmail> demandeEnCours(UUID compte) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT id_changement, nouvel_email, code, essais FROM changement_email
                WHERE account_id = :c AND date_confirmation IS NULL AND date_expiration > NOW()
                ORDER BY date_demande DESC LIMIT 1""").setParameter("c", compte).getResultList();
        return lignes.stream().findFirst().map(l -> new DemandeEmail(((Number) l[0]).longValue(), (String) l[1],
                (String) l[2], ((Number) l[3]).intValue()));
    }

    @Transactional
    public void essaiEmail(Long id) {
        em.createNativeQuery("UPDATE changement_email SET essais = essais + 1 WHERE id_changement = :id")
                .setParameter("id", id).executeUpdate();
    }

    @Transactional
    public void confirmerEmail(Long id, UUID compte, String email) {
        em.createNativeQuery("UPDATE changement_email SET date_confirmation = NOW() WHERE id_changement = :id")
                .setParameter("id", id).executeUpdate();
        em.createNativeQuery("UPDATE accounts SET email = :e, updated_at = NOW() WHERE id = :c")
                .setParameter("e", email).setParameter("c", compte).executeUpdate();
        // Profil (user-service, même base) : l'email affiché suit l'identifiant de connexion
        em.createNativeQuery("UPDATE utilisateurs SET email = :e, date_modification = NOW() WHERE account_id = :c")
                .setParameter("e", email).setParameter("c", compte).executeUpdate();
    }

    @Transactional(readOnly = true)
    public boolean emailPris(String email, UUID saufCompte) {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM accounts WHERE LOWER(email) = LOWER(:e) AND id <> :c")
                .setParameter("e", email).setParameter("c", saufCompte).getSingleResult()).longValue() > 0;
    }

    /** Profil utilisateur du compte (pour les notifications et le journal). */
    @Transactional(readOnly = true)
    public Optional<Long> utilisateurId(UUID compte) {
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT id_utilisateur FROM utilisateurs WHERE account_id = :c")
                .setParameter("c", compte).getResultList();
        return r.stream().findFirst().map(v -> ((Number) v).longValue());
    }

    private static UUID uuid(String s) {
        try {
            return s == null || s.isBlank() ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static LocalDateTime date(Object o) {
        if (o instanceof LocalDateTime d) return d;
        if (o instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }
}
