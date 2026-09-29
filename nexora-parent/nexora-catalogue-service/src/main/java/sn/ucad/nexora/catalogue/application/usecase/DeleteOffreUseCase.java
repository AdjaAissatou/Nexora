package sn.ucad.nexora.catalogue.application.usecase;

import java.util.UUID;

public interface DeleteOffreUseCase {
    void supprimer(UUID accountId, Long idOffre);
}
