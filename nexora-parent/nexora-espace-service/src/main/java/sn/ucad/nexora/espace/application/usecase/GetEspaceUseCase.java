package sn.ucad.nexora.espace.application.usecase;
import java.util.List; import java.util.UUID; import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
public interface GetEspaceUseCase{EspaceProfessionnel get(Long id); List<EspaceProfessionnel> mine(UUID accountId);}
