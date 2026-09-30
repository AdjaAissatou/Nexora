package sn.ucad.nexora.web.dto.espace;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos} (§10). Heures de Dakar. */
public final class HorairesDtos {

    private HorairesDtos() {}

    public record PlageDto(String jour, boolean ouvert, boolean ouvert24h, LocalTime ouverture, LocalTime fermeture,
                           LocalTime pauseDebut, LocalTime pauseFin) {}

    public record SemaineRequest(List<PlageDto> semaine) {}

    public record ExceptionRequest(LocalDate date, boolean ferme, LocalTime ouverture, LocalTime fermeture, String motif) {}

    public record JourResponse(String jour, String libelleJour, boolean ouvert, boolean ouvert24h, LocalTime ouverture,
                               LocalTime fermeture, LocalTime pauseDebut, LocalTime pauseFin, String resume,
                               boolean aujourdhui) implements Serializable {}

    public record ExceptionResponse(Long id, LocalDate date, boolean ferme, LocalTime ouverture, LocalTime fermeture,
                                    String motif, String resume) implements Serializable {}

    public record HorairesResponse(boolean renseignes, Boolean ouvertMaintenant, String etat, List<JourResponse> semaine,
                                   List<ExceptionResponse> exceptions) implements Serializable {}
}
