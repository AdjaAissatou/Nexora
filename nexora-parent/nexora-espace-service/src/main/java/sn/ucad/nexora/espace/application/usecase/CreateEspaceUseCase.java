package sn.ucad.nexora.espace.application.usecase;
import java.util.UUID; import sn.ucad.nexora.espace.application.dto.request.CreateEspaceRequest; import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
public interface CreateEspaceUseCase{EspaceProfessionnel create(UUID accountId,CreateEspaceRequest request);}
