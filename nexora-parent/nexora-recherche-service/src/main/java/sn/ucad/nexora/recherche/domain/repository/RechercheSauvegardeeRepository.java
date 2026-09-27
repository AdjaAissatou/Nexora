package sn.ucad.nexora.recherche.domain.repository;

import sn.ucad.nexora.recherche.domain.entity.RechercheSauvegardee;
import java.util.List;
import java.util.Optional;

public interface RechercheSauvegardeeRepository {
    RechercheSauvegardee save(RechercheSauvegardee recherche);
    Optional<RechercheSauvegardee> findById(Long id);
    List<RechercheSauvegardee> findByUtilisateurId(Long utilisateurId);
    void delete(Long id);
}
