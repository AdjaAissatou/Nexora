package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.AjouterFavoriRequest;
import sn.ucad.nexora.recherche.application.dto.response.FavoriResponse;
import sn.ucad.nexora.recherche.application.usecase.FavoriUseCase;
import sn.ucad.nexora.recherche.domain.entity.Favori;
import sn.ucad.nexora.recherche.domain.repository.FavoriRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriService implements FavoriUseCase {

    private final FavoriRepository repository;

    public FavoriService(FavoriRepository repository) {
        this.repository = repository;
    }

    @Override
    public FavoriResponse ajouter(Long utilisateurId, AjouterFavoriRequest request) {
        if (request.getOffreId() == null && request.getEspaceId() == null) {
            throw new IllegalArgumentException("offreId ou espaceId est requis");
        }

        // Idempotent : ne pas dupliquer
        if (request.getOffreId() != null &&
                repository.existsByUtilisateurIdAndOffreId(utilisateurId, request.getOffreId())) {
            return repository.findByUtilisateurIdAndOffreId(utilisateurId, request.getOffreId())
                    .map(this::toResponse)
                    .orElseThrow();
        }
        if (request.getEspaceId() != null &&
                repository.existsByUtilisateurIdAndEspaceId(utilisateurId, request.getEspaceId())) {
            return repository.findByUtilisateurIdAndEspaceId(utilisateurId, request.getEspaceId())
                    .map(this::toResponse)
                    .orElseThrow();
        }

        Favori f = new Favori();
        f.setUtilisateurId(utilisateurId);
        f.setOffreId(request.getOffreId());
        f.setEspaceId(request.getEspaceId());
        f.setDateCreation(LocalDateTime.now());
        return toResponse(repository.save(f));
    }

    @Override
    public void supprimer(Long utilisateurId, Long offreId, Long espaceId) {
        Favori f = null;
        if (offreId != null) {
            f = repository.findByUtilisateurIdAndOffreId(utilisateurId, offreId)
                    .orElseThrow(() -> new IllegalArgumentException("Favori introuvable"));
        } else if (espaceId != null) {
            f = repository.findByUtilisateurIdAndEspaceId(utilisateurId, espaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Favori introuvable"));
        } else {
            throw new IllegalArgumentException("offreId ou espaceId est requis");
        }
        repository.delete(f);
    }

    @Override
    public List<FavoriResponse> lister(Long utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FavoriResponse toResponse(Favori f) {
        FavoriResponse r = new FavoriResponse();
        r.setId(f.getId());
        r.setOffreId(f.getOffreId());
        r.setEspaceId(f.getEspaceId());
        r.setDateCreation(f.getDateCreation());
        return r;
    }
}
