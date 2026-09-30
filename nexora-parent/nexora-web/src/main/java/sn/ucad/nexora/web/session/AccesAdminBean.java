package sn.ucad.nexora.web.session;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Qui voit quoi dans le back-office (docs/architecture-acteurs.md §9.2), exposé à l'EL sous
 * {@code #{acces.xxx}} pour le menu, et gardes {@code preRenderView} des pages {@code /admin/...}.
 * Ce n'est qu'un confort d'affichage : chaque service revérifie le rôle sur ses propres routes.
 */
@Named("acces")
@RequestScoped
public class AccesAdminBean {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ADMIN = "ADMIN";
    private static final String MODERATEUR = "MODERATEUR";
    private static final String SUPPORT = "SUPPORT";
    private static final String GESTIONNAIRE = "GESTIONNAIRE";

    @Inject
    private SessionBean session;

    /** Un des rôles du back-office ({@code AGENT_VERIFICATION} n'en fait pas partie). */
    public boolean isBackOffice() {
        return session.aLeRole(SUPER_ADMIN, ADMIN, MODERATEUR, SUPPORT, GESTIONNAIRE);
    }

    public boolean isUtilisateurs() {
        return session.aLeRole(SUPER_ADMIN, ADMIN, MODERATEUR, SUPPORT);
    }

    /** Espaces, offres, avis, signalements. */
    public boolean isModeration() {
        return session.aLeRole(SUPER_ADMIN, ADMIN, MODERATEUR);
    }

    public boolean isVerifications() {
        return session.aLeRole(SUPER_ADMIN, ADMIN);
    }

    /** Catalogue, paramètres, statistiques. */
    public boolean isGestion() {
        return session.aLeRole(SUPER_ADMIN, ADMIN, GESTIONNAIRE);
    }

    public boolean isJournal() {
        return session.aLeRole(SUPER_ADMIN, ADMIN);
    }

    /** Donner ou retirer les rôles administratifs : réservé au super administrateur. */
    public boolean isRolesAdministratifs() {
        return session.aLeRole(SUPER_ADMIN);
    }

    private static final java.util.Map<String, String> LIBELLES_ROLES = java.util.Map.of(
            SUPER_ADMIN, "Super administrateur", ADMIN, "Administrateur", MODERATEUR, "Modérateur",
            SUPPORT, "Support", GESTIONNAIRE, "Gestionnaire");

    /** Rôles administratifs du compte, en clair, pour l'en-tête du back-office. */
    public String getLibelleRoles() {
        if (session.getCompte() == null || session.getCompte().roles() == null) return "";
        return LIBELLES_ROLES.entrySet().stream()
                .filter(e -> session.getCompte().roles().contains(e.getKey()))
                .map(java.util.Map.Entry::getValue).sorted()
                .collect(java.util.stream.Collectors.joining(", "));
    }

    // ------------------------------------------------------------------ gardes des pages

    public void exigerBackOffice() {
        session.exigerRole(session.isConnecte() && isBackOffice());
    }

    public void exigerVerifications() {
        session.exigerRole(session.isConnecte() && isVerifications());
    }

    public void exigerJournal() {
        session.exigerRole(session.isConnecte() && isJournal());
    }
}
