package sn.ucad.nexora.web.dto.administration;

import java.util.List;

/** Miroir de {@code AdministrationDtos.TableauDeBordResponse} (administration-service). */
public record TableauDeBordResponse(
        long comptes, long comptesSuspendus, long nouveauxComptes30Jours,
        long espaces, long espacesActifs, long espacesSuspendus, long espacesVerifies,
        long offres, long offresPubliees, long offresSuspendues,
        long verificationsEnAttente, long verificationsEnCours,
        long signalementsEnAttente, long avis,
        List<ActionJournalResponse> dernieresActions) {}
