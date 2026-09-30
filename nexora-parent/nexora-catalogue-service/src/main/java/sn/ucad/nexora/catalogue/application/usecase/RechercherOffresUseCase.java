package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.response.OffrePageResponse;

public interface RechercherOffresUseCase {
    OffrePageResponse rechercher(OffreSearchRequest request);

    /** Toutes les offres d'un espace (statut et motif de modération compris), pour son seul propriétaire. */
    OffrePageResponse gestion(java.util.UUID accountId, Long idEspace);
}
