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
