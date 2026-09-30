package sn.ucad.nexora.web.dto.administration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Miroirs de {@code AdminComptesDtos} (auth-service) : comptes, rôles, permissions. */
public final class AdminComptesDtos {

    private AdminComptesDtos() {}

    /** {@code etat} : ACTIF, SUSPENDU, NON_VERIFIE ou DESACTIVE. */
    public record CompteResume(UUID accountId, Long utilisateurId, String prenom, String nom, String email,
                               String telephone, List<String> roles, String etat, LocalDateTime dateCreation,
                               long nombreEspaces) {
        public String nomComplet() {
            return ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
        }
    }

    public record PageComptes(List<CompteResume> comptes, int page, boolean pageSuivante, long total) {}

    public record EspaceDuCompte(Long id, String nom, String statut, boolean verifie) {}

    public record ActionHistorique(LocalDateTime date, String auteur, String action, String description) {}

    public record FicheCompte(CompteResume compte, List<String> permissions, List<EspaceDuCompte> espaces,
                              LocalDateTime derniereConnexion, List<ActionHistorique> historique) {}

    public record PermissionInfo(String code, String nom, String description, String module, boolean actif) {}

    public record RoleInfo(String code, String nom, String description, boolean actif, boolean administratif,
                           long nombreComptes, List<String> permissions) {}

    public record RolesEtPermissions(List<RoleInfo> roles, List<PermissionInfo> permissions) {}

    public record MotifRequest(String motif) {}
}
