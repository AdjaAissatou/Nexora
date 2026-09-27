package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.request.OffreSearchRequest;
import sn.ucad.nexora.catalogue.application.dto.response.OffrePageResponse;

public interface RechercherOffresUseCase {
    OffrePageResponse rechercher(OffreSearchRequest request);
}
