package sn.ucad.nexora.recherche.domain.repository;

import sn.ucad.nexora.recherche.domain.entity.Avis;
import java.util.List;
import java.util.Optional;

public interface AvisRepository {
    Avis save(Avis avis);
    Optional<Avis> findById(Long id);
    List<Avis> findByOffreId(Long offreId);
    List<Avis> findByEspaceId(Long espaceId);
    List<Avis> findByUtilisateurId(Long utilisateurId);
    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);
    void delete(Long id);
    void flush();
}
