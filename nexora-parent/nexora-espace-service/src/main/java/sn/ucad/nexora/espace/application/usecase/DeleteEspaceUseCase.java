package sn.ucad.nexora.espace.application.usecase;

import java.util.UUID;

public interface DeleteEspaceUseCase {
    void supprimer(UUID accountId, Long espaceId);
}
