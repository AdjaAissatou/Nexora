package sn.ucad.nexora.espace.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Rôle FOURNISSEUR : un compte l'a tant qu'il possède au moins un espace professionnel
 * (cf. docs/architecture-acteurs.md §1). Requêtes natives, cohérent avec
 * GeoQueryRepository — account_roles/roles appartiennent au bounded context
 * auth-service mais partagent la même base physique que les autres services.
 */
@Component
public class RoleAssignmentRepository {

    private final EntityManager em;

    public RoleAssignmentRepository(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void attribuerFournisseurSiAbsent(UUID accountId) {
        em.createNativeQuery("""
                INSERT INTO account_roles (account_id, role_id)
                SELECT :accountId, r.id FROM roles r WHERE r.code = 'FOURNISSEUR'
                ON CONFLICT (account_id, role_id) DO NOTHING
                """)
                .setParameter("accountId", accountId)
                .executeUpdate();
    }

    @Transactional
    public void retirerFournisseurSiPlusAucunEspace(UUID accountId) {
        em.createNativeQuery("""
                DELETE FROM account_roles ar
                USING roles r
                WHERE ar.role_id = r.id AND r.code = 'FOURNISSEUR' AND ar.account_id = :accountId
                  AND NOT EXISTS (
                      SELECT 1 FROM espace_professionnel e
                      JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur
                      WHERE u.account_id = :accountId)
                """)
                .setParameter("accountId", accountId)
                .executeUpdate();
    }
}
