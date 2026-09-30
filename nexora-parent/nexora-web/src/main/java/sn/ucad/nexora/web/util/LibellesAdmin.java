package sn.ucad.nexora.web.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;

/** Libellés du back-office exposés à l'EL ({@code #{libAdmin.xxx}}) : états de compte, rôles. */
@Named("libAdmin")
@ApplicationScoped
public class LibellesAdmin implements Serializable {

    private static final Map<String, String> ETATS = Map.of(
            "ACTIF", "Actif", "SUSPENDU", "Suspendu", "NON_VERIFIE", "Inscription non confirmée", "DESACTIVE", "Désactivé");

    private static final Map<String, String> ROLES = Map.of(
            "SUPER_ADMIN", "Super administrateur", "ADMIN", "Administrateur", "MODERATEUR", "Modérateur",
            "SUPPORT", "Support", "GESTIONNAIRE", "Gestionnaire", "AGENT_VERIFICATION", "Agent de vérification",
            "FOURNISSEUR", "Professionnel", "UTILISATEUR", "Utilisateur");

    public String etat(String code) {
        return code == null ? "" : ETATS.getOrDefault(code, code);
    }

    public String classeEtat(String code) {
        if (code == null) return "nx-badge-neutre";
        return switch (code) {
            case "ACTIF" -> "nx-badge-verifie";
            case "SUSPENDU" -> "nx-badge-alerte";
            case "NON_VERIFIE" -> "nx-badge-attente";
            default -> "nx-badge-neutre";
        };
    }

    public String role(String code) {
        return code == null ? "" : ROLES.getOrDefault(code, code);
    }

    public java.util.List<String> getEtats() {
        return java.util.List.of("ACTIF", "SUSPENDU", "NON_VERIFIE", "DESACTIVE");
    }
}
