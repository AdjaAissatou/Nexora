package sn.ucad.nexora.web.dto.auth;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.auth.application.dto.securite.SecuriteDtos} (§25). */
public final class SecuriteDtos {

    private SecuriteDtos() {}

    public record Connexion(String idSession, LocalDateTime debut, LocalDateTime derniereActivite, String adresseIp,
                            String appareil, boolean active, boolean actuelle, LocalDateTime fin, String motifFin)
            implements Serializable {}

    public record TentativeEchouee(LocalDateTime date, String adresseIp, String appareil) implements Serializable {}

    public record Securite(String email, String telephone, List<Connexion> sessionsActives, List<Connexion> historique,
                           List<TentativeEchouee> tentativesEchouees, String emailEnAttente) implements Serializable {}

    public record Resultat(String message, int sessionsFermees) implements Serializable {}
}
