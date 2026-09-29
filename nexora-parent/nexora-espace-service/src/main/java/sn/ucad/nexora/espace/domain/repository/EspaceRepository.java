package sn.ucad.nexora.espace.domain.repository;

import java.util.List;
import java.util.Optional;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;

public interface EspaceRepository {
    EspaceProfessionnel save(EspaceProfessionnel espace);
    Optional<EspaceProfessionnel> findById(Long id);
    List<EspaceProfessionnel> findByUtilisateurId(Long utilisateurId);
    void deleteById(Long id);
}
