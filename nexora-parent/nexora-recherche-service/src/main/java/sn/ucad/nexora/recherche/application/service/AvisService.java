package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.PublierAvisRequest;
import sn.ucad.nexora.recherche.application.dto.response.AvisResponse;
import sn.ucad.nexora.recherche.application.usecase.AvisUseCase;
import sn.ucad.nexora.recherche.domain.entity.Avis;
import sn.ucad.nexora.recherche.domain.repository.AvisRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvisService implements AvisUseCase {

    private final AvisRepository repository;

    public AvisService(AvisRepository repository) {
        this.repository = repository;
    }

    @Override
    public AvisResponse publier(Long utilisateurId, PublierAvisRequest request) {
        if (request.getOffreId() == null && request.getEspaceId() == null) {
            throw new IllegalArgumentException("offreId ou espaceId est requis");
        }
        if (request.getOffreId() != null &&
                repository.existsByUtilisateurIdAndOffreId(utilisateurId, request.getOffreId())) {
            throw new IllegalArgumentException("Vous avez déjà publié un avis sur cette offre");
        }

        Avis a = new Avis();
        a.setUtilisateurId(utilisateurId);
        a.setOffreId(request.getOffreId());
        a.setEspaceId(request.getEspaceId());
        a.setNote(request.getNote());
        a.setCommentaire(request.getCommentaire());
        a.setDateCreation(LocalDateTime.now());
        return toResponse(repository.save(a));
    }

    @Override
    public List<AvisResponse> listerParOffre(Long offreId) {
        return repository.findByOffreId(offreId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<AvisResponse> listerParEspace(Long espaceId) {
        return repository.findByEspaceId(espaceId).stream().map(this::toResponse).toList();
    }

    @Override
    public void supprimer(Long utilisateurId, Long avisId) {
        Avis a = repository.findById(avisId)
                .orElseThrow(() -> new IllegalArgumentException("Avis introuvable"));
        if (!a.getUtilisateurId().equals(utilisateurId)) {
            throw new IllegalArgumentException("Action non autorisée");
        }
        repository.delete(avisId);
    }

    private AvisResponse toResponse(Avis a) {
        AvisResponse r = new AvisResponse();
        r.setId(a.getId());
        r.setUtilisateurId(a.getUtilisateurId());
        r.setOffreId(a.getOffreId());
        r.setEspaceId(a.getEspaceId());
        r.setNote(a.getNote());
        r.setCommentaire(a.getCommentaire());
        r.setReponseFournisseur(a.getReponseFournisseur());
        r.setDateCreation(a.getDateCreation());
        r.setDateReponse(a.getDateReponse());
        return r;
    }
}
