package sn.ucad.nexora.recherche.domain.repository;

import sn.ucad.nexora.recherche.domain.entity.HistoriqueRecherche;
import java.util.List;

public interface HistoriqueRechercheRepository {
    HistoriqueRecherche save(HistoriqueRecherche historique);
    List<HistoriqueRecherche> findByUtilisateurId(Long utilisateurId);
    void deleteByUtilisateurId(Long utilisateurId);
}
