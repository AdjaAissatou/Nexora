package sn.ucad.nexora.recherche.domain.repository;

import sn.ucad.nexora.recherche.domain.entity.Favori;
import java.util.List;
import java.util.Optional;

public interface FavoriRepository {
    Favori save(Favori favori);
    Optional<Favori> findByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);
    Optional<Favori> findByUtilisateurIdAndEspaceId(Long utilisateurId, Long espaceId);
    List<Favori> findByUtilisateurId(Long utilisateurId);
    void delete(Favori favori);
    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);
    boolean existsByUtilisateurIdAndEspaceId(Long utilisateurId, Long espaceId);
}
