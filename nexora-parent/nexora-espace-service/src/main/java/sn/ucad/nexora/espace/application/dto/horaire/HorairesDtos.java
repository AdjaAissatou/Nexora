package sn.ucad.nexora.espace.application.dto.horaire;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Requêtes et réponses des horaires d'un espace (docs/architecture-acteurs.md §10). Heures de Dakar. */
public final class HorairesDtos {

    private HorairesDtos() {}

    /** Une journée : {@code jour} = LUNDI … DIMANCHE. */
    public record PlageDto(String jour, boolean ouvert, boolean ouvert24h, LocalTime ouverture, LocalTime fermeture,
                           LocalTime pauseDebut, LocalTime pauseFin) {}

    /** La semaine type complète ; une liste vide efface les horaires. */
    public record SemaineRequest(List<PlageDto> semaine) {}

    public record ExceptionRequest(LocalDate date, boolean ferme, LocalTime ouverture, LocalTime fermeture, String motif) {}

    public record JourResponse(String jour, String libelleJour, boolean ouvert, boolean ouvert24h, LocalTime ouverture,
                               LocalTime fermeture, LocalTime pauseDebut, LocalTime pauseFin, String resume,
                               boolean aujourdhui) {}

    public record ExceptionResponse(Long id, LocalDate date, boolean ferme, LocalTime ouverture, LocalTime fermeture,
                                    String motif, String resume) {}

    /** {@code ouvertMaintenant} : null si les horaires ne sont pas renseignés. */
    public record HorairesResponse(boolean renseignes, Boolean ouvertMaintenant, String etat, List<JourResponse> semaine,
                                   List<ExceptionResponse> exceptions) {}
}
