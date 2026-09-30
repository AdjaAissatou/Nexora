package sn.ucad.nexora.auth.application.dto.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Réponses et requêtes de l'administration des comptes, rôles et permissions (docs/architecture-acteurs.md §9). */
public final class AdminComptesDtos {

    private AdminComptesDtos() {}

    /**
     * État d'un compte, déduit de accounts : SUSPENDU (locked), DESACTIVE (enabled = faux),
     * NON_VERIFIE (inscription non confirmée), ACTIF.
     */
    public record CompteResume(UUID accountId, Long utilisateurId, String prenom, String nom, String email,
                               String telephone, List<String> roles, String etat, LocalDateTime dateCreation,
                               long nombreEspaces) {}

    public record PageComptes(List<CompteResume> comptes, int page, boolean pageSuivante, long total) {}

    public record EspaceDuCompte(Long id, String nom, String statut, boolean verifie) {}

    public record ActionHistorique(LocalDateTime date, String auteur, String action, String description) {}

    /** Fiche d'un compte : ses rôles, les permissions qu'ils lui donnent, ses espaces, l'historique d'administration. */
    public record FicheCompte(CompteResume compte, List<String> permissions, List<EspaceDuCompte> espaces,
                              LocalDateTime derniereConnexion, List<ActionHistorique> historique) {}

    public record PermissionInfo(String code, String nom, String description, String module, boolean actif) {}

    public record RoleInfo(String code, String nom, String description, boolean actif, boolean administratif,
                           long nombreComptes, List<String> permissions) {}

    /** Matrice rôles × permissions. */
    public record RolesEtPermissions(List<RoleInfo> roles, List<PermissionInfo> permissions) {}

    public record MotifRequest(String motif) {}
}
