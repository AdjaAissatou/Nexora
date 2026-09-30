package sn.ucad.nexora.espace.infrastructure.persistence.verification;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Lectures et écritures natives hors du contexte « espace » dont la vérification a besoin :
 * noms des utilisateurs, rôles (auth-service), type d'espace, notifications. Même base physique
 * partagée, même convention que RoleAssignmentRepository / GeoQueryRepository.
 */
@Component
public class VerificationLookupRepository {

    private final EntityManager em;

    public VerificationLookupRepository(EntityManager em) {
        this.em = em;
    }

    public record Personne(Long id, String nomComplet, String email, String telephone) {}

    public Map<Long, Personne> personnes(Collection<Long> ids) {
        Map<Long, Personne> resultat = new HashMap<>();
        List<Long> filtres = ids.stream().filter(id -> id != null).distinct().toList();
        if (filtres.isEmpty()) return resultat;
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT id_utilisateur, TRIM(COALESCE(prenom, '') || ' ' || COALESCE(nom, '')), email, telephone
                FROM utilisateurs WHERE id_utilisateur IN (:ids)
                """).setParameter("ids", filtres).getResultList();
        for (Object[] l : lignes) {
            Long id = ((Number) l[0]).longValue();
            resultat.put(id, new Personne(id, (String) l[1], (String) l[2], (String) l[3]));
        }
        return resultat;
    }

    public Optional<String> nomTypeEspace(Long typeEspaceId) {
        @SuppressWarnings("unchecked")
        List<String> noms = em.createNativeQuery("SELECT nom FROM type_espace WHERE id_type_espace = :id")
                .setParameter("id", typeEspaceId).getResultList();
        return noms.stream().findFirst();
    }

    /** Vrai si le compte de cet utilisateur a le rôle donné (table account_roles d'auth-service). */
    public boolean aLeRole(Long utilisateurId, String codeRole) {
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM utilisateurs u
                JOIN account_roles ar ON ar.account_id = u.account_id
                JOIN roles r ON r.id = ar.role_id
                WHERE u.id_utilisateur = :id AND r.code = :code AND r.active
                """).setParameter("id", utilisateurId).setParameter("code", codeRole).getSingleResult();
        return n.longValue() > 0;
    }

    /** Agent de vérification : identité et nombre de demandes qu'il suit encore (EN_COURS, A_COMPLETER). */
    public record Agent(Long id, String nomComplet, String email, long demandesOuvertes) {}

    public List<Agent> agents() {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT u.id_utilisateur, TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')), u.email,
                       (SELECT COUNT(*) FROM verification_espace v
                        WHERE v.id_agent = u.id_utilisateur AND v.statut IN ('EN_COURS', 'A_COMPLETER'))
                FROM utilisateurs u
                JOIN account_roles ar ON ar.account_id = u.account_id
                JOIN roles r ON r.id = ar.role_id AND r.code = 'AGENT_VERIFICATION'
                ORDER BY 2
                """).getResultList();
        return lignes.stream().map(l -> new Agent(((Number) l[0]).longValue(), (String) l[1], (String) l[2],
                ((Number) l[3]).longValue())).toList();
    }

    public Optional<Long> utilisateurParEmail(String email) {
        @SuppressWarnings("unchecked")
        List<Number> ids = em.createNativeQuery("SELECT id_utilisateur FROM utilisateurs WHERE LOWER(email) = LOWER(:email)")
                .setParameter("email", email.trim()).getResultList();
        return ids.stream().findFirst().map(Number::longValue);
    }

    /** Ajoute ou retire un rôle au compte de l'utilisateur (account_roles d'auth-service). */
    public void attribuerRole(Long utilisateurId, String codeRole) {
        em.createNativeQuery("""
                INSERT INTO account_roles (account_id, role_id)
                SELECT u.account_id, r.id FROM utilisateurs u, roles r
                WHERE u.id_utilisateur = :id AND r.code = :code
                ON CONFLICT (account_id, role_id) DO NOTHING
                """).setParameter("id", utilisateurId).setParameter("code", codeRole).executeUpdate();
    }

    public void retirerRole(Long utilisateurId, String codeRole) {
        em.createNativeQuery("""
                DELETE FROM account_roles ar
                USING utilisateurs u, roles r
                WHERE ar.account_id = u.account_id AND ar.role_id = r.id
                  AND u.id_utilisateur = :id AND r.code = :code
                """).setParameter("id", utilisateurId).setParameter("code", codeRole).executeUpdate();
    }

    /** Notification applicative (table notification, type CERTIFICATION déjà prévu par l'enum). */
    public void notifier(Long utilisateurId, String titre, String message, String urlAction) {
        em.createNativeQuery("""
                INSERT INTO notification (id_utilisateur, titre, message, type, url_action)
                VALUES (:id, :titre, :message, 'CERTIFICATION', :url)
                """)
                .setParameter("id", utilisateurId)
                .setParameter("titre", titre)
                .setParameter("message", message)
                .setParameter("url", urlAction)
                .executeUpdate();
    }
}
