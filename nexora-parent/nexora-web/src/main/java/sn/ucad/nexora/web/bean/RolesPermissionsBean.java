package sn.ucad.nexora.web.bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sn.ucad.nexora.web.client.AdminComptesApiClient;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.PermissionInfo;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.RoleInfo;
import sn.ucad.nexora.web.dto.administration.AdminComptesDtos.RolesEtPermissions;
import sn.ucad.nexora.web.error.ApiException;
import sn.ucad.nexora.web.session.SessionBean;

/**
 * Matrice rôles × permissions ({@code admin/roles.xhtml}), docs/architecture-acteurs.md §6 : lecture
 * pour tout le back-office, modification avec la permission GERER_PERMISSIONS.
 */
@Named
@ViewScoped
public class RolesPermissionsBean implements Serializable {

    /** Un groupe de permissions d'un même module, pour l'affichage. */
    public record Module(String nom, List<PermissionInfo> permissions) implements Serializable {}

    private static final Map<String, String> MODULES = Map.of(
            "ADMINISTRATION", "Administration", "SECURITY", "Comptes et sécurité", "PROFESSIONAL", "Espaces professionnels",
            "CATALOGUE", "Catalogue", "SEARCH", "Recherche, avis, signalements", "COMMUNICATION", "Communication",
            "COMMERCE", "Commerce");

    @Inject
    private transient AdminComptesApiClient adminComptesApiClient;

    @Inject
    private SessionBean session;

    private RolesEtPermissions matrice;
    private List<Module> modules = List.of();
    private String erreur;
    private String motif;

    @PostConstruct
    public void charger() {
        try {
            appliquer(adminComptesApiClient.rolesEtPermissions(session.getAccessToken()));
        } catch (ApiException e) {
            erreur = e.getMessage();
        }
    }

    private void appliquer(RolesEtPermissions m) {
        matrice = m;
        Map<String, List<PermissionInfo>> parModule = new LinkedHashMap<>();
        for (PermissionInfo p : m.permissions()) {
            parModule.computeIfAbsent(p.module() == null ? "AUTRE" : p.module(), k -> new java.util.ArrayList<>()).add(p);
        }
        modules = parModule.entrySet().stream()
                .map(e -> new Module(MODULES.getOrDefault(e.getKey(), e.getKey()), e.getValue())).toList();
    }

    /** Accorde ou retire une permission à un rôle (motif obligatoire, journalisé par auth-service). */
    public void basculer(String role, String permission) {
        boolean accorder = !aLaPermission(role, permission);
        try {
            appliquer(adminComptesApiClient.modifierPermission(session.getAccessToken(), role, permission, accorder, motif));
            motif = null;
            message(FacesMessage.SEVERITY_INFO, "Permission " + permission + (accorder ? " accordée au" : " retirée du")
                    + " rôle " + role + ". Effet au plus tard dans 15 minutes pour les comptes concernés.");
        } catch (ApiException e) {
            message(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public boolean aLaPermission(String role, String permission) {
        return matrice != null && matrice.roles().stream()
                .anyMatch(r -> r.code().equals(role) && r.permissions().contains(permission));
    }

    private static void message(FacesMessage.Severity gravite, String texte) {
        FacesContext.getCurrentInstance().addMessage(null, sn.ucad.nexora.web.util.Messages.complet(gravite, texte, null));
    }

    public List<RoleInfo> getRoles() { return matrice == null ? List.of() : matrice.roles(); }
    public List<Module> getModules() { return modules; }
    public String getErreur() { return erreur; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
