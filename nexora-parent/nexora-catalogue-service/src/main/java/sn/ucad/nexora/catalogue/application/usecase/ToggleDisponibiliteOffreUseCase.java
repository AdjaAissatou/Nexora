package sn.ucad.nexora.catalogue.application.usecase;

import java.util.UUID;

public interface ToggleDisponibiliteOffreUseCase {
    void basculer(UUID accountId, Long idOffre, boolean disponible);
}
