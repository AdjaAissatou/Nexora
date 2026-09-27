package sn.ucad.nexora.recherche.domain.repository;

import sn.ucad.nexora.recherche.domain.entity.HistoriqueConsultation;
import java.util.List;

public interface HistoriqueConsultationRepository {
    HistoriqueConsultation save(HistoriqueConsultation consultation);
    List<HistoriqueConsultation> findByUtilisateurId(Long utilisateurId);
    void deleteByUtilisateurId(Long utilisateurId);
}
