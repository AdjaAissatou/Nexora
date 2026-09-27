package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.SauvegarderRechercheRequest;
import sn.ucad.nexora.recherche.application.dto.response.RechercheSauvegardeeResponse;
import sn.ucad.nexora.recherche.application.usecase.RechercheSauvegardeeUseCase;
import sn.ucad.nexora.recherche.domain.entity.RechercheSauvegardee;
import sn.ucad.nexora.recherche.domain.repository.RechercheSauvegardeeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RechercheSauvegardeeService implements RechercheSauvegardeeUseCase {

    private final RechercheSauvegardeeRepository repository;

    public RechercheSauvegardeeService(RechercheSauvegardeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public RechercheSauvegardeeResponse sauvegarder(Long utilisateurId, SauvegarderRechercheRequest request) {
        RechercheSauvegardee rs = new RechercheSauvegardee();
        rs.setUtilisateurId(utilisateurId);
        rs.setNom(request.getNom());
        rs.setMotCle(request.getMotCle());
        rs.setCategorie(request.getCategorie());
        rs.setRayonKm(request.getRayonKm());
        rs.setLatitude(request.getLatitude());
        rs.setLongitude(request.getLongitude());
        rs.setActive(true);
        rs.setDateCreation(LocalDateTime.now());
        return toResponse(repository.save(rs));
    }

    @Override
    public List<RechercheSauvegardeeResponse> lister(Long utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId).stream().map(this::toResponse).toList();
    }

    @Override
    public void supprimer(Long utilisateurId, Long id) {
        RechercheSauvegardee rs = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recherche introuvable"));
        if (!rs.getUtilisateurId().equals(utilisateurId)) {
            throw new IllegalArgumentException("Action non autorisée");
        }
        repository.delete(id);
    }

    private RechercheSauvegardeeResponse toResponse(RechercheSauvegardee rs) {
        RechercheSauvegardeeResponse r = new RechercheSauvegardeeResponse();
        r.setId(rs.getId());
        r.setNom(rs.getNom());
        r.setMotCle(rs.getMotCle());
        r.setCategorie(rs.getCategorie());
        r.setRayonKm(rs.getRayonKm());
        r.setLatitude(rs.getLatitude());
        r.setLongitude(rs.getLongitude());
        r.setActive(rs.isActive());
        r.setDateCreation(rs.getDateCreation());
        return r;
    }
}
