package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.recherche.domain.entity.HistoriqueRecherche;
import sn.ucad.nexora.recherche.domain.repository.HistoriqueRechercheRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.HistoriqueRechercheJpaEntity;

import java.util.List;

@Component
public class HistoriqueRechercheRepositoryAdapter implements HistoriqueRechercheRepository {

    private final HistoriqueRechercheJpaRepository jpa;

    public HistoriqueRechercheRepositoryAdapter(HistoriqueRechercheJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public HistoriqueRecherche save(HistoriqueRecherche h) {
        return toDomain(jpa.save(toEntity(h)));
    }

    @Override
    public List<HistoriqueRecherche> findByUtilisateurId(Long utilisateurId) {
        return jpa.findByUtilisateurIdOrderByDateRechercheDesc(utilisateurId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteByUtilisateurId(Long utilisateurId) {
        jpa.deleteByUtilisateurId(utilisateurId);
    }

    private HistoriqueRechercheJpaEntity toEntity(HistoriqueRecherche h) {
        HistoriqueRechercheJpaEntity e = new HistoriqueRechercheJpaEntity();
        e.setId(h.getId());
        e.setUtilisateurId(h.getUtilisateurId());
        e.setMotCle(h.getMotCle());
        e.setCategorie(h.getCategorie());
        e.setLatitude(h.getLatitude());
        e.setLongitude(h.getLongitude());
        e.setRayonKm(h.getRayonKm());
        e.setNombreResultats(h.getNombreResultats());
        e.setDateRecherche(h.getDateRecherche());
        return e;
    }

    private HistoriqueRecherche toDomain(HistoriqueRechercheJpaEntity e) {
        HistoriqueRecherche h = new HistoriqueRecherche();
        h.setId(e.getId());
        h.setUtilisateurId(e.getUtilisateurId());
        h.setMotCle(e.getMotCle());
        h.setCategorie(e.getCategorie());
        h.setLatitude(e.getLatitude());
        h.setLongitude(e.getLongitude());
        h.setRayonKm(e.getRayonKm());
        h.setNombreResultats(e.getNombreResultats());
        h.setDateRecherche(e.getDateRecherche());
        return h;
    }
}
