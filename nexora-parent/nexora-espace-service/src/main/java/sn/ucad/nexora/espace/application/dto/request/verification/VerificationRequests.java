package sn.ucad.nexora.espace.application.dto.request.verification;

/** Corps des requêtes de l'API de vérification. */
public final class VerificationRequests {

    private VerificationRequests() {}

    /** Demande d'informations, refus, annulation, révocation : un motif, obligatoire selon l'action. */
    public record MotifRequest(String motif) {}

    /** Résultat d'un point de contrôle : CONFORME, NON_CONFORME ou NON_FAIT. */
    public record ControleRequest(String resultat, String commentaire) {}

    /** Examen d'un justificatif : ACCEPTE ou REJETE (motif obligatoire si rejeté). */
    public record ExamenDocumentRequest(String decision, String motif) {}

    /** Réattribution : l'utilisateur agent visé, ou null pour remettre la demande dans la file. */
    public record ReattributionRequest(Long agentUtilisateurId, String motif) {}

    /** Désignation d'un agent par l'e-mail de son compte Nexora. */
    public record AgentRequest(String email) {}
}
