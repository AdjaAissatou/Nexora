package sn.ucad.nexora.administration.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Réponses de l'API d'administration transverse (docs/architecture-acteurs.md §9). */
public final class AdministrationDtos {

    private AdministrationDtos() {}

    /** Compteurs du tableau de bord, lus en direct sur la base partagée. */
    public record TableauDeBordResponse(
            long comptes, long comptesSuspendus, long nouveauxComptes30Jours,
            long espaces, long espacesActifs, long espacesSuspendus, long espacesVerifies,
            long offres, long offresPubliees, long offresSuspendues,
            long verificationsEnAttente, long verificationsEnCours,
            long signalementsEnAttente, long avis,
            /** Vide pour les rôles qui n'ont pas accès au journal. */
            List<ActionJournalResponse> dernieresActions) {}

    public record ActionJournalResponse(Long id, LocalDateTime date, String auteurNom, String auteurEmail,
                                        String module, String action, String entite, Long idEntite,
                                        String description, String adresseIp) {}

    public record PageJournalResponse(List<ActionJournalResponse> actions, int page, boolean pageSuivante,
                                      List<String> modules) {}
}
