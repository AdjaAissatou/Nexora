package sn.ucad.nexora.web.session;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Qui voit quoi dans le back-office, exposé à l'EL sous {@code #{acces.xxx}} pour le menu, et gardes
 * {@code preRenderView} des pages {@code /admin/...}. Tout repose sur les PERMISSIONS effectives du
 * compte (docs/architecture-acteurs.md §6), jamais sur des noms de rôles : changer la matrice rôles ×
 * permissions change aussi ce que montre l'interface. Ce n'est qu'un confort d'affichage : chaque
 * service revérifie la permission sur ses propres routes.
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

    public boolean isBackOffice() {
        return session.aLaPermission("ACCEDER_BACK_OFFICE");
    }

    public boolean isUtilisateurs() {
        return session.aLaPermission("CONSULTER_UTILISATEURS");
    }

    public boolean isSuspendre() {
        return session.aLaPermission("SUSPENDRE_UTILISATEURS");
    }

    public boolean isReactiver() {
        return session.aLaPermission("REACTIVER_UTILISATEURS");
    }

    /** Donner ou retirer les rôles administratifs d'un compte. */
    public boolean isRolesAdministratifs() {
        return session.aLaPermission("GERER_ROLES");
    }

    /** Modifier la matrice rôles × permissions. */
    public boolean isPermissions() {
        return session.aLaPermission("GERER_PERMISSIONS");
    }

    /** Espaces, offres, avis, signalements. */
    public boolean isModeration() {
        return session.aLaPermission("MODERER_ESPACES", "MODERER_OFFRES", "MODERER_AVIS", "GERER_SIGNALEMENTS");
    }

    public boolean isModererEspaces() {
        return session.aLaPermission("MODERER_ESPACES");
    }

    public boolean isModererOffres() {
        return session.aLaPermission("MODERER_OFFRES");
    }

    public boolean isSignalements() {
        return session.aLaPermission("GERER_SIGNALEMENTS");
    }

    public boolean isModererAvis() {
        return session.aLaPermission("MODERER_AVIS");
    }

    /** Catalogue : l'une des trois permissions (catégories, types d'offre, attributs), §9.11. */
    public boolean isCatalogue() {
        return session.aLaPermission("GERER_CATEGORIES", "GERER_TYPES_OFFRES", "GERER_ATTRIBUTS");
    }

    public boolean isCategories() {
        return session.aLaPermission("GERER_CATEGORIES");
    }

    public boolean isTypesOffre() {
        return session.aLaPermission("GERER_TYPES_OFFRES");
    }

    public boolean isAttributs() {
        return session.aLaPermission("GERER_ATTRIBUTS");
    }

    public boolean isVerifications() {
        return session.aLaPermission("SUPERVISER_VERIFICATIONS");
    }

    /** Catalogue, paramètres, statistiques. */
    public boolean isGestion() {
        return session.aLaPermission("GERER_CATEGORIES", "GERER_TYPES_OFFRES", "GERER_ATTRIBUTS", "GERER_PARAMETRES");
    }

    /** Paramètres de Nexora et règles des justificatifs, §9.12. */
    public boolean isParametres() {
        return session.aLaPermission("GERER_PARAMETRES");
    }

    public boolean isJournal() {
        return session.aLaPermission("GERER_JOURNAL");
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

    public void exigerUtilisateurs() {
        session.exigerRole(session.isConnecte() && isUtilisateurs());
    }

    public void exigerModererEspaces() {
        session.exigerRole(session.isConnecte() && isModererEspaces());
    }

    public void exigerModererOffres() {
        session.exigerRole(session.isConnecte() && isModererOffres());
    }

    public void exigerCatalogue() {
        session.exigerRole(session.isConnecte() && isCatalogue());
    }

    public void exigerSignalements() {
        session.exigerRole(session.isConnecte() && isSignalements());
    }

    public void exigerParametres() {
        session.exigerRole(session.isConnecte() && isParametres());
    }

    public void exigerModererAvis() {
        session.exigerRole(session.isConnecte() && isModererAvis());
    }
}
