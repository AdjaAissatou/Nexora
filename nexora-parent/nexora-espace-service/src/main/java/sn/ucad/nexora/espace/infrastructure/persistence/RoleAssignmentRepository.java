package sn.ucad.nexora.espace.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Attribution du rôle FOURNISSEUR : un compte le devient dès qu'il possède au moins
 * un espace professionnel (cf. docs/architecture-acteurs.md §1). Requête native,
 * cohérent avec GeoQueryRepository — account_roles/roles appartiennent au bounded
 * context auth-service mais partagent la même base physique que les autres services.
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
}
