package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse;

public interface GetOffreUseCase {
    OffreDetailResponse get(Long id);
}
