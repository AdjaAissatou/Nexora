package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.EnregistrerConsultationRequest;
import sn.ucad.nexora.recherche.application.usecase.ConsultationUseCase;
import sn.ucad.nexora.recherche.domain.entity.HistoriqueConsultation;
import sn.ucad.nexora.recherche.domain.repository.HistoriqueConsultationRepository;

import java.time.LocalDateTime;

@Service
public class ConsultationService implements ConsultationUseCase {

    private final HistoriqueConsultationRepository repository;

    public ConsultationService(HistoriqueConsultationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void enregistrer(Long utilisateurId, EnregistrerConsultationRequest request) {
        if (request.getOffreId() == null && request.getEspaceId() == null) {
            throw new IllegalArgumentException("offreId ou espaceId est requis");
        }
        HistoriqueConsultation c = new HistoriqueConsultation();
        c.setUtilisateurId(utilisateurId);
        c.setOffreId(request.getOffreId());
        c.setEspaceId(request.getEspaceId());
        c.setDureeSecondes(request.getDureeSecondes());
        c.setDateConsultation(LocalDateTime.now());
        repository.save(c);
    }

    @Override
    public void effacer(Long utilisateurId) {
        repository.deleteByUtilisateurId(utilisateurId);
    }
}
