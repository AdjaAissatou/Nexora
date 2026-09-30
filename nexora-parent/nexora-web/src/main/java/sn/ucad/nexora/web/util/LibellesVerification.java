package sn.ucad.nexora.web.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;

/**
 * Libellés et classes CSS de la vérification des espaces, exposés à l'EL ({@code #{verif.xxx}}).
 * Les codes viennent d'espace-service (docs/architecture-acteurs.md §8).
 */
@Named("verif")
@ApplicationScoped
public class LibellesVerification implements Serializable {

    private static final Map<String, String> STATUTS = Map.of(
            "BROUILLON", "En préparation",
            "EN_ATTENTE", "En attente",
            "EN_COURS", "En cours d'examen",
            "A_COMPLETER", "Informations demandées",
            "APPROUVEE", "Approuvée",
            "REFUSEE", "Refusée",
            "ANNULEE", "Annulée",
            "REVOQUEE", "Révoquée");

    /** État présenté au professionnel, déduit de la dernière demande. */
    private static final Map<String, String> ETATS = Map.of(
            "NON_VERIFIE", "Non vérifié",
            "EN_COURS", "Vérification en cours",
            "A_COMPLETER", "Informations demandées",
            "VERIFIE", "Vérifié",
            "REFUSEE", "Vérification refusée",
            "REVOQUEE", "Vérification retirée");

    private static final Map<String, String> EVENEMENTS = Map.ofEntries(
            Map.entry("CREATION", "Demande créée"),
            Map.entry("DOCUMENT_DEPOSE", "Document reçu"),
            Map.entry("DOCUMENT_RETIRE", "Document retiré"),
            Map.entry("SOUMISSION", "Demande envoyée"),
            Map.entry("RESOUMISSION", "Dossier complété et renvoyé"),
            Map.entry("PRISE_EN_CHARGE", "Prise en charge par un agent"),
            Map.entry("DOCUMENT_EXAMINE", "Document examiné"),
            Map.entry("CONTROLE", "Point contrôlé"),
            Map.entry("INFOS_DEMANDEES", "Informations demandées"),
            Map.entry("APPROBATION", "Vérification approuvée"),
            Map.entry("REFUS", "Vérification refusée"),
            Map.entry("ANNULATION", "Demande annulée"),
            Map.entry("REATTRIBUTION", "Demande réattribuée"),
            Map.entry("REVOCATION", "Vérification retirée"),
            Map.entry("MODIFICATION_ESPACE", "Informations de l'espace modifiées"),
            Map.entry("REPRISE", "Vérifié avant le processus actuel"));

    private static final Map<String, String> DOCUMENTS = Map.of(
            "DEPOSE", "En attente d'examen",
            "ACCEPTE", "Accepté",
            "REJETE", "Rejeté",
            "REMPLACE", "Remplacé");

    private static final Map<String, String> RESULTATS = Map.of(
            "NON_FAIT", "À contrôler",
            "CONFORME", "Conforme",
            "NON_CONFORME", "Non conforme");

    private static final Map<String, String> ROLES = Map.of(
            "PROFESSIONNEL", "Professionnel",
            "AGENT", "Agent de vérification",
            "ADMIN", "Administrateur",
            "SYSTEME", "Nexora");

    public String statut(String code) {
        return libelle(STATUTS, code);
    }

    public String etat(String code) {
        return libelle(ETATS, code);
    }

    public String evenement(String code) {
        return libelle(EVENEMENTS, code);
    }

    public String document(String code) {
        return libelle(DOCUMENTS, code);
    }

    public String resultat(String code) {
        return libelle(RESULTATS, code);
    }

    public String role(String code) {
        return libelle(ROLES, code);
    }

    /** Classe de badge selon la tonalité : succès, attente, alerte, neutre. */
    public String classe(String code) {
        if (code == null) return "nx-badge-neutre";
        return switch (code) {
            case "APPROUVEE", "VERIFIE", "ACCEPTE", "CONFORME" -> "nx-badge-verifie";
            case "EN_ATTENTE", "EN_COURS", "DEPOSE" -> "nx-badge-attente";
            case "A_COMPLETER", "REFUSEE", "REVOQUEE", "REJETE", "NON_CONFORME" -> "nx-badge-alerte";
            default -> "nx-badge-neutre";
        };
    }

    private static String libelle(Map<String, String> table, String code) {
        return code == null ? "" : table.getOrDefault(code, code);
    }
}
