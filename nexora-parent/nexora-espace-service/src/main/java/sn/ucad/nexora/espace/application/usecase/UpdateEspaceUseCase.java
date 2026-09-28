package sn.ucad.nexora.espace.application.usecase;

import java.util.UUID;
import sn.ucad.nexora.espace.application.dto.request.UpdateEspaceRequest;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;

public interface UpdateEspaceUseCase {
    EspaceProfessionnel update(UUID accountId, Long espaceId, UpdateEspaceRequest request);
}
