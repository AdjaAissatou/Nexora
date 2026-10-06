package sn.ucad.nexora.catalogue.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.LieuPublicResponse;
import sn.ucad.nexora.catalogue.application.usecase.LieuPublicUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.LieuPublicQueryRepository;

import java.util.List;

@Service
public class LieuPublicService implements LieuPublicUseCase {

    private final LieuPublicQueryRepository repository;

    public LieuPublicService(LieuPublicQueryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LieuPublicResponse> rechercher(String q, String commune, String typeLieu, int limite) {
        int limiteBornee = Math.min(Math.max(limite, 1), 500);
        return repository.rechercher(q, commune, typeLieu, limiteBornee);
    }

    @Override
    public java.util.Optional<LieuPublicResponse> parId(Long id) {
        return repository.parId(id);
    }
}
