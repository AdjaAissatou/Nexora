package sn.ucad.nexora.auth.application.service.admin;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.CompteResume;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.FicheCompte;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.PageComptes;
import sn.ucad.nexora.auth.application.dto.admin.AdminComptesDtos.RolesEtPermissions;
import sn.ucad.nexora.auth.infrastructure.persistance.admin.ComptesAdminRepository;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;

/**
 * Administration des comptes, de leurs rôles et de la matrice rôles × permissions
 * (docs/architecture-acteurs.md §6, §9.2). Les permissions requises sont vérifiées par
 * SecurityConfig ; ce service applique les règles métier et journalise chaque action.
 */
@Service
public class AdministrationComptesService {

    static final int TAILLE_PAGE = 25;

    /** Rôles donnés à la main par un super administrateur. Les autres rôles ont leur propre cycle :
     * UTILISATEUR (inscription), FOURNISSEUR (possession d'un espace), AGENT_VERIFICATION (§8). */
    public static final Set<String> ROLES_ADMINISTRATIFS = Set.of("SUPER_ADMIN", "ADMIN", "MODERATEUR", "SUPPORT", "GESTIONNAIRE");

    /** Permissions que le rôle SUPER_ADMIN ne peut pas perdre : sinon plus personne ne pourrait rien corriger. */
    static final Set<String> PERMISSIONS_VITALES_SUPER_ADMIN = Set.of("ACCEDER_BACK_OFFICE", "GERER_ROLES", "GERER_PERMISSIONS");

    private static final Set<String> ETATS = Set.of("ACTIF", "SUSPENDU", "NON_VERIFIE", "DESACTIVE");

    private final ComptesAdminRepository comptes;
    private final JournalActions journal;

    public AdministrationComptesService(ComptesAdminRepository comptes, JournalActions journal) {
        this.comptes = comptes;
        this.journal = journal;
    }

    // ------------------------------------------------------------------ consultation

    @Transactional(readOnly = true)
    public PageComptes rechercher(String recherche, String role, String etat, int page) {
        String r = nettoyer(recherche);
        String ro = nettoyer(role);
        String e = nettoyer(etat);
        if (e != null && !ETATS.contains(e)) throw new BusinessException("État de compte inconnu : " + e);
        int p = Math.max(page, 0);
        List<CompteResume> lignes = comptes.rechercher(r, ro, e, TAILLE_PAGE + 1, p * TAILLE_PAGE);
        boolean suivante = lignes.size() > TAILLE_PAGE;
        return new PageComptes(suivante ? lignes.subList(0, TAILLE_PAGE) : lignes, p, suivante, comptes.compter(r, ro, e));
    }

    @Transactional(readOnly = true)
    public FicheCompte fiche(UUID accountId) {
        CompteResume c = compte(accountId);
        return new FicheCompte(c, comptes.permissionsEffectives(accountId), comptes.espaces(c.utilisateurId()),
                comptes.derniereConnexion(c.utilisateurId()), comptes.historique(c.utilisateurId()));
    }

    // ------------------------------------------------------------------ suspension

    /**
     * Suspend un compte : connexion et renouvellement du jeton refusés, donc session web fermée au
     * plus tard 15 minutes après (durée du jeton d'accès).
     */
    @Transactional
    public FicheCompte suspendre(UUID auteur, boolean auteurSuperAdmin, UUID cible, String motif) {
        exigerMotif(motif);
        exigerAutreCompte(auteur, cible, "suspendre");
        CompteResume c = compte(cible);
        if ("SUSPENDU".equals(c.etat())) throw new BusinessException("Ce compte est déjà suspendu");
        if (c.roles().contains("SUPER_ADMIN") && !auteurSuperAdmin) {
            throw new BusinessException("Seul un super administrateur peut suspendre un super administrateur");
        }
        if (c.roles().contains("SUPER_ADMIN") && comptes.comptesActifsAvecRole("SUPER_ADMIN") <= 1) {
            throw new BusinessException("Impossible de suspendre le dernier super administrateur actif");
        }
        comptes.definirSuspension(cible, true);
        journaliser(auteur, "SUSPENDRE_COMPTE", c, "Compte " + c.email() + " suspendu : " + motif.trim());
        return fiche(cible);
    }

    @Transactional
    public FicheCompte reactiver(UUID auteur, UUID cible, String motif) {
        exigerMotif(motif);
        exigerAutreCompte(auteur, cible, "réactiver");
        CompteResume c = compte(cible);
        if (!"SUSPENDU".equals(c.etat())) throw new BusinessException("Ce compte n'est pas suspendu");
        comptes.definirSuspension(cible, false);
        journaliser(auteur, "REACTIVER_COMPTE", c, "Compte " + c.email() + " réactivé : " + motif.trim());
        return fiche(cible);
    }

    // ------------------------------------------------------------------ rôles administratifs

    @Transactional
    public FicheCompte donnerRole(UUID auteur, UUID cible, String role, String motif) {
        exigerMotif(motif);
        exigerRoleAdministratif(role);
        exigerAutreCompte(auteur, cible, "modifier les rôles de");
        CompteResume c = compte(cible);
        if (c.roles().contains(role)) throw new BusinessException("Ce compte a déjà le rôle " + role);
        comptes.ajouterRole(cible, role);
        journaliser(auteur, "DONNER_ROLE", c, "Rôle " + role + " donné à " + c.email() + " : " + motif.trim());
        return fiche(cible);
    }

    @Transactional
    public FicheCompte retirerRole(UUID auteur, UUID cible, String role, String motif) {
        exigerMotif(motif);
        exigerRoleAdministratif(role);
        exigerAutreCompte(auteur, cible, "modifier les rôles de");
        CompteResume c = compte(cible);
        if (!c.roles().contains(role)) throw new BusinessException("Ce compte n'a pas le rôle " + role);
        if ("SUPER_ADMIN".equals(role) && comptes.comptesActifsAvecRole("SUPER_ADMIN") <= 1) {
            throw new BusinessException("Impossible de retirer le rôle au dernier super administrateur actif");
        }
        comptes.retirerRole(cible, role);
        journaliser(auteur, "RETIRER_ROLE", c, "Rôle " + role + " retiré à " + c.email() + " : " + motif.trim());
        return fiche(cible);
    }

    // ------------------------------------------------------------------ matrice rôles × permissions

    @Transactional(readOnly = true)
    public RolesEtPermissions rolesEtPermissions() {
        return new RolesEtPermissions(comptes.roles(ROLES_ADMINISTRATIFS), comptes.permissions());
    }

    /** Relie ou délie une permission d'un rôle. Prend effet au prochain renouvellement du jeton des comptes concernés. */
    @Transactional
    public RolesEtPermissions modifierPermission(UUID auteur, String role, String permission, boolean accorder, String motif) {
        exigerMotif(motif);
        if (!comptes.roleExiste(role)) throw new ResourceNotFoundException("Rôle introuvable : " + role);
        if (!comptes.permissionExiste(permission)) throw new ResourceNotFoundException("Permission introuvable : " + permission);
        if (!accorder && "SUPER_ADMIN".equals(role) && PERMISSIONS_VITALES_SUPER_ADMIN.contains(permission)) {
            throw new BusinessException("La permission " + permission + " ne peut pas être retirée au rôle SUPER_ADMIN");
        }
        int modifiees = accorder ? comptes.lierPermission(role, permission) : comptes.delierPermission(role, permission);
        if (modifiees == 0) {
            throw new BusinessException(accorder ? "Le rôle " + role + " a déjà cette permission"
                    : "Le rôle " + role + " n'a pas cette permission");
        }
        journal.enregistrer(auteur, "PERMISSIONS", accorder ? "ACCORDER_PERMISSION" : "RETIRER_PERMISSION", "role", null,
                "Permission " + permission + (accorder ? " accordée au" : " retirée du") + " rôle " + role + " : " + motif.trim());
        return rolesEtPermissions();
    }

    // ------------------------------------------------------------------ outils

    private CompteResume compte(UUID accountId) {
        return comptes.compte(accountId).orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
    }

    private void journaliser(UUID auteur, String action, CompteResume cible, String description) {
        journal.enregistrer(auteur, "COMPTES", action, "utilisateur", cible.utilisateurId(), description);
    }

    private static void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
    }

    private static void exigerAutreCompte(UUID auteur, UUID cible, String verbe) {
        if (auteur != null && auteur.equals(cible)) {
            throw new BusinessException("Vous ne pouvez pas " + verbe + " votre propre compte");
        }
    }

    private static void exigerRoleAdministratif(String role) {
        if (!ROLES_ADMINISTRATIFS.contains(role)) {
            throw new BusinessException("Seuls les rôles administratifs se donnent ici : "
                    + String.join(", ", ROLES_ADMINISTRATIFS.stream().sorted().toList()));
        }
    }

    private static String nettoyer(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
