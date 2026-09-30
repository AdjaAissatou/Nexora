package sn.ucad.nexora.auth.infrastructure.persistance.admin;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.ActionHistorique;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.CompteResume;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.EspaceDuCompte;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.PermissionInfo;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.RoleInfo;

/**
 * Lectures et écritures natives de l'administration des comptes. accounts, roles, permissions et
 * leurs liaisons appartiennent à auth-service ; utilisateurs, espace_professionnel et journal_action
 * sont lus sur la base partagée (même convention que les *LookupRepository des autres services).
 */
@Component
public class ComptesAdminRepository {

    /**
     * Expression SQL de l'état d'un compte (voir CompteResume), insérée à la place de {ETAT}. Pas de
     * String.formatted ici : les requêtes contiennent des '%' (ILIKE) qu'il prendrait pour des formats.
     */
    private static final String ETAT = """
            CASE WHEN a.locked THEN 'SUSPENDU' WHEN NOT a.enabled THEN 'DESACTIVE'
                 WHEN NOT a.verified THEN 'NON_VERIFIE' ELSE 'ACTIF' END""";

    private static final String SELECT_COMPTE = """
            SELECT a.id, u.id_utilisateur, a.first_name, a.last_name, a.email, a.phone,
                   (SELECT string_agg(r.code, ',' ORDER BY r.code) FROM account_roles ar
                    JOIN roles r ON r.id = ar.role_id WHERE ar.account_id = a.id),
                   {ETAT}, a.created_at,
                   (SELECT COUNT(*) FROM espace_professionnel e WHERE e.id_utilisateur = u.id_utilisateur)
            FROM accounts a
            LEFT JOIN utilisateurs u ON u.account_id = a.id
            """.replace("{ETAT}", ETAT);

    private final EntityManager em;

    public ComptesAdminRepository(EntityManager em) {
        this.em = em;
    }

    public List<CompteResume> rechercher(String recherche, String role, String etat, int limite, int decalage) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery(SELECT_COMPTE + """
                WHERE (CAST(:recherche AS TEXT) IS NULL
                       OR a.email ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR a.phone ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR (a.first_name || ' ' || a.last_name) ILIKE '%' || CAST(:recherche AS TEXT) || '%')
                  AND (CAST(:role AS TEXT) IS NULL OR EXISTS (
                       SELECT 1 FROM account_roles ar JOIN roles r ON r.id = ar.role_id
                       WHERE ar.account_id = a.id AND r.code = CAST(:role AS TEXT)))
                  AND (CAST(:etat AS TEXT) IS NULL OR {ETAT} = CAST(:etat AS TEXT))
                ORDER BY a.created_at DESC, a.email
                LIMIT :limite OFFSET :decalage
                """.replace("{ETAT}", ETAT))
                .setParameter("recherche", recherche)
                .setParameter("role", role)
                .setParameter("etat", etat)
                .setParameter("limite", limite)
                .setParameter("decalage", decalage)
                .getResultList();
        return lignes.stream().map(ComptesAdminRepository::compte).toList();
    }

    public long compter(String recherche, String role, String etat) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM accounts a
                WHERE (CAST(:recherche AS TEXT) IS NULL
                       OR a.email ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR a.phone ILIKE '%' || CAST(:recherche AS TEXT) || '%'
                       OR (a.first_name || ' ' || a.last_name) ILIKE '%' || CAST(:recherche AS TEXT) || '%')
                  AND (CAST(:role AS TEXT) IS NULL OR EXISTS (
                       SELECT 1 FROM account_roles ar JOIN roles r ON r.id = ar.role_id
                       WHERE ar.account_id = a.id AND r.code = CAST(:role AS TEXT)))
                  AND (CAST(:etat AS TEXT) IS NULL OR {ETAT} = CAST(:etat AS TEXT))
                """.replace("{ETAT}", ETAT))
                .setParameter("recherche", recherche)
                .setParameter("role", role)
                .setParameter("etat", etat)
                .getSingleResult()).longValue();
    }

    public Optional<CompteResume> compte(UUID accountId) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery(SELECT_COMPTE + " WHERE a.id = :id")
                .setParameter("id", accountId).getResultList();
        return lignes.stream().findFirst().map(ComptesAdminRepository::compte);
    }

    /** Permissions effectives : celles des rôles actifs du compte, permissions actives uniquement. */
    @SuppressWarnings("unchecked")
    public List<String> permissionsEffectives(UUID accountId) {
        return em.createNativeQuery("""
                SELECT DISTINCT p.code FROM account_roles ar
                JOIN roles r ON r.id = ar.role_id AND r.active
                JOIN role_permissions rp ON rp.role_id = r.id
                JOIN permissions p ON p.id = rp.permission_id AND p.active
                WHERE ar.account_id = :id ORDER BY p.code
                """).setParameter("id", accountId).getResultList();
    }

    public List<EspaceDuCompte> espaces(Long utilisateurId) {
        if (utilisateurId == null) return List.of();
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT id_espace, nom, CAST(statut AS TEXT), COALESCE(verifie, FALSE)
                FROM espace_professionnel WHERE id_utilisateur = :id ORDER BY nom
                """).setParameter("id", utilisateurId).getResultList();
        return lignes.stream().map(l -> new EspaceDuCompte(((Number) l[0]).longValue(), (String) l[1],
                (String) l[2], Boolean.TRUE.equals(l[3]))).toList();
    }

    public LocalDateTime derniereConnexion(Long utilisateurId) {
        if (utilisateurId == null) return null;
        @SuppressWarnings("unchecked")
        List<Object> r = em.createNativeQuery("SELECT derniere_connexion FROM utilisateurs WHERE id_utilisateur = :id")
                .setParameter("id", utilisateurId).getResultList();
        return r.isEmpty() ? null : date(r.get(0));
    }

    /** Actions d'administration portant sur ce compte, les plus récentes d'abord. */
    public List<ActionHistorique> historique(Long utilisateurId) {
        if (utilisateurId == null) return List.of();
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT j.date_action, TRIM(COALESCE(u.prenom, '') || ' ' || COALESCE(u.nom, '')), j.action, j.description
                FROM journal_action j LEFT JOIN utilisateurs u ON u.id_utilisateur = j.id_utilisateur
                WHERE j.entite = 'utilisateur' AND j.id_entite = :id
                ORDER BY j.date_action DESC, j.id_journal_action DESC LIMIT 50
                """).setParameter("id", utilisateurId).getResultList();
        return lignes.stream().map(l -> new ActionHistorique(date(l[0]), (String) l[1], (String) l[2], (String) l[3]))
                .toList();
    }

    // ------------------------------------------------------------------ écritures

    /** Suspension (locked) : la connexion et le renouvellement du jeton sont refusés par auth-service. */
    public void definirSuspension(UUID accountId, boolean suspendu) {
        em.createNativeQuery("UPDATE accounts SET locked = :s, updated_at = NOW() WHERE id = :id")
                .setParameter("s", suspendu).setParameter("id", accountId).executeUpdate();
        // Le profil (user-service) porte aussi un statut : on le garde cohérent.
        em.createNativeQuery("""
                UPDATE utilisateurs SET compte_bloque = :s,
                       statut = CAST(CASE WHEN :s THEN 'SUSPENDU' ELSE 'ACTIF' END AS statut_compte),
                       date_modification = NOW()
                WHERE account_id = :id
                """).setParameter("s", suspendu).setParameter("id", accountId).executeUpdate();
    }

    public void ajouterRole(UUID accountId, String codeRole) {
        em.createNativeQuery("""
                INSERT INTO account_roles (account_id, role_id)
                SELECT :id, r.id FROM roles r WHERE r.code = :code
                ON CONFLICT (account_id, role_id) DO NOTHING
                """).setParameter("id", accountId).setParameter("code", codeRole).executeUpdate();
    }

    public void retirerRole(UUID accountId, String codeRole) {
        em.createNativeQuery("""
                DELETE FROM account_roles ar USING roles r
                WHERE ar.role_id = r.id AND r.code = :code AND ar.account_id = :id
                """).setParameter("id", accountId).setParameter("code", codeRole).executeUpdate();
    }

    /** Comptes non suspendus ayant ce rôle (garde-fou : ne jamais retirer le dernier super administrateur). */
    public long comptesActifsAvecRole(String codeRole) {
        return ((Number) em.createNativeQuery("""
                SELECT COUNT(*) FROM account_roles ar JOIN roles r ON r.id = ar.role_id
                JOIN accounts a ON a.id = ar.account_id
                WHERE r.code = :code AND NOT a.locked AND a.enabled
                """).setParameter("code", codeRole).getSingleResult()).longValue();
    }

    // ------------------------------------------------------------------ rôles et permissions

    public List<RoleInfo> roles(java.util.Set<String> rolesAdministratifs) {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT r.code, r.name, r.description, r.active,
                       (SELECT COUNT(*) FROM account_roles ar WHERE ar.role_id = r.id),
                       (SELECT string_agg(p.code, ',' ORDER BY p.code) FROM role_permissions rp
                        JOIN permissions p ON p.id = rp.permission_id WHERE rp.role_id = r.id)
                FROM roles r ORDER BY r.code
                """).getResultList();
        return lignes.stream().map(l -> new RoleInfo((String) l[0], (String) l[1], (String) l[2],
                Boolean.TRUE.equals(l[3]), rolesAdministratifs.contains((String) l[0]),
                ((Number) l[4]).longValue(), liste((String) l[5]))).toList();
    }

    public List<PermissionInfo> permissions() {
        @SuppressWarnings("unchecked")
        List<Object[]> lignes = em.createNativeQuery(
                "SELECT code, name, description, module, active FROM permissions ORDER BY module, code").getResultList();
        return lignes.stream().map(l -> new PermissionInfo((String) l[0], (String) l[1], (String) l[2],
                (String) l[3], Boolean.TRUE.equals(l[4]))).toList();
    }

    public boolean roleExiste(String code) {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM roles WHERE code = :c")
                .setParameter("c", code).getSingleResult()).longValue() > 0;
    }

    public boolean permissionExiste(String code) {
        return ((Number) em.createNativeQuery("SELECT COUNT(*) FROM permissions WHERE code = :c")
                .setParameter("c", code).getSingleResult()).longValue() > 0;
    }

    public int lierPermission(String role, String permission) {
        return em.createNativeQuery("""
                INSERT INTO role_permissions (role_id, permission_id)
                SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = :r AND p.code = :p
                ON CONFLICT DO NOTHING
                """).setParameter("r", role).setParameter("p", permission).executeUpdate();
    }

    public int delierPermission(String role, String permission) {
        return em.createNativeQuery("""
                DELETE FROM role_permissions rp USING roles r, permissions p
                WHERE rp.role_id = r.id AND rp.permission_id = p.id AND r.code = :r AND p.code = :p
                """).setParameter("r", role).setParameter("p", permission).executeUpdate();
    }

    // ------------------------------------------------------------------ outils

    private static CompteResume compte(Object[] l) {
        return new CompteResume((UUID) l[0], l[1] == null ? null : ((Number) l[1]).longValue(), (String) l[2],
                (String) l[3], (String) l[4], (String) l[5], liste((String) l[6]), (String) l[7], date(l[8]),
                ((Number) l[9]).longValue());
    }

    private static List<String> liste(String valeurs) {
        return valeurs == null || valeurs.isBlank() ? List.of() : Arrays.asList(valeurs.split(","));
    }

    /** Selon Hibernate, une colonne TIMESTAMP native arrive en LocalDateTime ou en Timestamp. */
    private static LocalDateTime date(Object valeur) {
        if (valeur instanceof LocalDateTime d) return d;
        if (valeur instanceof Timestamp t) return t.toLocalDateTime();
        return null;
    }
}
