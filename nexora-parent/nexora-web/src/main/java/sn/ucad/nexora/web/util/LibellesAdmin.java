package sn.ucad.nexora.web.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;

/** Libellés du back-office exposés à l'EL ({@code #{libAdmin.xxx}}) : états de compte, rôles, statuts modérés. */
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

    // ------------------------------------------------------------------ modération (§9.9)

    private static final Map<String, String> STATUTS_ESPACE = Map.of(
            "ACTIF", "Actif", "SUSPENDU", "Suspendu", "BROUILLON", "Brouillon", "EN_ATTENTE", "En attente",
            "EN_VERIFICATION", "En vérification", "REFUSE", "Refusé", "FERME", "Fermé");

    private static final Map<String, String> STATUTS_OFFRE = Map.of(
            "PUBLIE", "Publiée", "SUSPENDU", "Suspendue", "BROUILLON", "Brouillon", "EXPIRE", "Expirée", "SUPPRIME", "Supprimée");

    private static final Map<String, String> ACTIONS = Map.of(
            "SUSPENDRE_ESPACE", "Espace suspendu", "REACTIVER_ESPACE", "Espace réactivé",
            "SUSPENDRE_OFFRE", "Offre suspendue", "REPUBLIER_OFFRE", "Offre republiée");

    public String statutEspace(String code) {
        return code == null ? "" : STATUTS_ESPACE.getOrDefault(code, code);
    }

    public String statutOffre(String code) {
        return code == null ? "" : STATUTS_OFFRE.getOrDefault(code, code);
    }

    /** Classe du badge d'un statut d'espace ou d'offre : vert si visible, rouge si suspendu. */
    public String classeStatut(String code) {
        if (code == null) return "nx-badge-neutre";
        return switch (code) {
            case "ACTIF", "PUBLIE" -> "nx-badge-verifie";
            case "SUSPENDU" -> "nx-badge-alerte";
            case "EN_ATTENTE", "EN_VERIFICATION", "BROUILLON" -> "nx-badge-attente";
            default -> "nx-badge-neutre";
        };
    }

    public String action(String code) {
        return code == null ? "" : ACTIONS.getOrDefault(code, code);
    }

    public java.util.List<String> getStatutsEspace() {
        return java.util.List.of("ACTIF", "SUSPENDU");
    }

    public java.util.List<String> getStatutsOffre() {
        return java.util.List.of("PUBLIE", "SUSPENDU");
    }
}
