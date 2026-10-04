package sn.ucad.nexora.catalogue.application.usecase;

import sn.ucad.nexora.catalogue.application.dto.response.LieuPublicResponse;

import java.util.List;

public interface LieuPublicUseCase {
    List<LieuPublicResponse> rechercher(String q, String commune, String typeLieu, int limite);

    java.util.Optional<LieuPublicResponse> parId(Long id);
}
