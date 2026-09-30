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

    private static final Map<String, String> STATUTS_SIGNALEMENT = Map.of(
            "EN_ATTENTE", "En attente", "EN_COURS", "Pris en charge", "TRAITE", "Traité", "REJETE", "Rejeté");

    private static final Map<String, String> TYPES_SIGNALEMENT = Map.of("ESPACE", "Espace", "OFFRE", "Offre", "AVIS", "Avis");

    /** Mêmes libellés que recherche-service (SignalementAdminService.MOTIFS). */
    private static final Map<String, String> MOTIFS_SIGNALEMENT = Map.of(
            "ARNAQUE", "Arnaque ou fraude", "INFORMATIONS_FAUSSES", "Informations fausses ou trompeuses",
            "CONTENU_INAPPROPRIE", "Contenu choquant ou inapproprié", "LIEU_INEXISTANT", "Le lieu n'existe pas ou est fermé",
            "AVIS_FAUX", "Avis faux ou malveillant", "AUTRE", "Autre");

    public String statutSignalement(String code) {
        return code == null ? "" : STATUTS_SIGNALEMENT.getOrDefault(code, code);
    }

    public String classeSignalement(String code) {
        if (code == null) return "nx-badge-neutre";
        return switch (code) {
            case "EN_ATTENTE" -> "nx-badge-alerte";
            case "EN_COURS" -> "nx-badge-attente";
            case "TRAITE" -> "nx-badge-verifie";
            default -> "nx-badge-neutre";
        };
    }

    public String typeSignalement(String code) {
        return code == null ? "" : TYPES_SIGNALEMENT.getOrDefault(code, code);
    }

    public String motifSignalement(String code) {
        return code == null ? "" : MOTIFS_SIGNALEMENT.getOrDefault(code, code);
    }

    public java.util.List<String> getStatutsSignalement() {
        return java.util.List.of("EN_ATTENTE", "EN_COURS", "TRAITE", "REJETE");
    }

    public java.util.List<String> getTypesSignalement() {
        return java.util.List.of("ESPACE", "OFFRE", "AVIS");
    }

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
