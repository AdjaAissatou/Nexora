package sn.ucad.nexora.recherche.application.service;

import org.springframework.stereotype.Service;
import sn.ucad.nexora.recherche.application.dto.request.EnregistrerRechercheRequest;
import sn.ucad.nexora.recherche.application.dto.response.HistoriqueRechercheResponse;
import sn.ucad.nexora.recherche.application.usecase.HistoriqueRechercheUseCase;
import sn.ucad.nexora.recherche.domain.entity.HistoriqueRecherche;
import sn.ucad.nexora.recherche.domain.repository.HistoriqueRechercheRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoriqueRechercheService implements HistoriqueRechercheUseCase {

    private final HistoriqueRechercheRepository repository;

    public HistoriqueRechercheService(HistoriqueRechercheRepository repository) {
        this.repository = repository;
    }

    @Override
    public void enregistrer(Long utilisateurId, EnregistrerRechercheRequest request) {
        HistoriqueRecherche h = new HistoriqueRecherche();
        h.setUtilisateurId(utilisateurId);
        h.setMotCle(request.getMotCle());
        h.setCategorie(request.getCategorie());
        h.setLatitude(request.getLatitude());
        h.setLongitude(request.getLongitude());
        h.setRayonKm(request.getRayonKm());
        h.setNombreResultats(request.getNombreResultats() != null ? request.getNombreResultats() : 0);
        h.setDateRecherche(LocalDateTime.now());
        repository.save(h);
    }

    @Override
    public List<HistoriqueRechercheResponse> lister(Long utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void effacer(Long utilisateurId) {
        repository.deleteByUtilisateurId(utilisateurId);
    }

    private HistoriqueRechercheResponse toResponse(HistoriqueRecherche h) {
        HistoriqueRechercheResponse r = new HistoriqueRechercheResponse();
        r.setId(h.getId());
        r.setMotCle(h.getMotCle());
        r.setCategorie(h.getCategorie());
        r.setLatitude(h.getLatitude());
        r.setLongitude(h.getLongitude());
        r.setRayonKm(h.getRayonKm());
        r.setNombreResultats(h.getNombreResultats());
        r.setDateRecherche(h.getDateRecherche());
        return r;
    }
}
