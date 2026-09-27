package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.recherche.domain.entity.Favori;
import sn.ucad.nexora.recherche.domain.repository.FavoriRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.FavoriJpaEntity;

import java.util.List;
import java.util.Optional;

@Component
public class FavoriRepositoryAdapter implements FavoriRepository {

    private final FavoriJpaRepository jpa;

    public FavoriRepositoryAdapter(FavoriJpaRepository jpa) { this.jpa = jpa; }

    @Override public Favori save(Favori f) { return toDomain(jpa.save(toEntity(f))); }

    @Override
    public Optional<Favori> findByUtilisateurIdAndOffreId(Long uid, Long oid) {
        return jpa.findByUtilisateurIdAndOffreId(uid, oid).map(this::toDomain);
    }

    @Override
    public Optional<Favori> findByUtilisateurIdAndEspaceId(Long uid, Long eid) {
        return jpa.findByUtilisateurIdAndEspaceId(uid, eid).map(this::toDomain);
    }

    @Override
    public List<Favori> findByUtilisateurId(Long uid) {
        return jpa.findByUtilisateurIdOrderByDateCreationDesc(uid).stream().map(this::toDomain).toList();
    }

    @Override public void delete(Favori f) { jpa.deleteById(f.getId()); }

    @Override
    public boolean existsByUtilisateurIdAndOffreId(Long uid, Long oid) {
        return jpa.existsByUtilisateurIdAndOffreId(uid, oid);
    }

    @Override
    public boolean existsByUtilisateurIdAndEspaceId(Long uid, Long eid) {
        return jpa.existsByUtilisateurIdAndEspaceId(uid, eid);
    }

    private FavoriJpaEntity toEntity(Favori f) {
        FavoriJpaEntity e = new FavoriJpaEntity();
        e.setId(f.getId()); e.setUtilisateurId(f.getUtilisateurId());
        e.setOffreId(f.getOffreId()); e.setEspaceId(f.getEspaceId());
        e.setDateCreation(f.getDateCreation());
        return e;
    }

    private Favori toDomain(FavoriJpaEntity e) {
        Favori f = new Favori();
        f.setId(e.getId()); f.setUtilisateurId(e.getUtilisateurId());
        f.setOffreId(e.getOffreId()); f.setEspaceId(e.getEspaceId());
        f.setDateCreation(e.getDateCreation());
        return f;
    }
}
