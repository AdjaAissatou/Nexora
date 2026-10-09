package sn.ucad.nexora.auth.application.dto.securite;

import java.time.LocalDateTime;
import java.util.List;

/** Sécurité du compte (docs/architecture-acteurs.md §25). */
public final class SecuriteDtos {

    private SecuriteDtos() {}

    /**
     * Une connexion. {@code active} : session en cours (pas de fin) ; {@code actuelle} : celle de la
     * requête ; {@code fin} et {@code motifFin} pour une session terminée.
     */
    public record Connexion(String idSession, LocalDateTime debut, LocalDateTime derniereActivite, String adresseIp,
                            String appareil, boolean active, boolean actuelle, LocalDateTime fin, String motifFin) {}

    public record TentativeEchouee(LocalDateTime date, String adresseIp, String appareil) {}

    /** {@code emailEnAttente} : nouvelle adresse dont le code n'est pas encore confirmé (null sinon). */
    public record Securite(String email, String telephone, List<Connexion> sessionsActives, List<Connexion> historique,
                           List<TentativeEchouee> tentativesEchouees, String emailEnAttente) {}

    public record ChangementMotDePasse(String actuel, String nouveau, String confirmation) {}

    public record DemandeChangementEmail(String nouvelEmail, String motDePasse) {}

    public record ConfirmationEmail(String code) {}

    public record Resultat(String message, int sessionsFermees) {}
}
