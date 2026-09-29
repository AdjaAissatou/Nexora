package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.request.UpdateOffreRequest;
import sn.ucad.nexora.catalogue.application.dto.response.CreateOffreResponse;

import java.util.UUID;

public interface UpdateOffreUseCase {
    CreateOffreResponse modifier(UUID accountId, Long idOffre, UpdateOffreRequest request);
}
