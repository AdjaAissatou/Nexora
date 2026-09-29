package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.response.OffreEditionResponse;

import java.util.UUID;

public interface GetOffreEditionUseCase {
    OffreEditionResponse obtenir(UUID accountId, Long idOffre);
}
