package sn.ucad.nexora.recherche.domain.repository;

import sn.ucad.nexora.recherche.domain.entity.Signalement;
import java.util.List;

public interface SignalementRepository {
    Signalement save(Signalement signalement);
    List<Signalement> findByUtilisateurId(Long utilisateurId);
    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);
}
