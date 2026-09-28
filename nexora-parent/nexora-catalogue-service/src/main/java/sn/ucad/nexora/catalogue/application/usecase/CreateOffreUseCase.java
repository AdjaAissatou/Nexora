package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.request.CreateOffreRequest;
import sn.ucad.nexora.catalogue.application.dto.response.CreateOffreResponse;

import java.util.UUID;

public interface CreateOffreUseCase {
    CreateOffreResponse creer(UUID accountId, CreateOffreRequest request);
}
