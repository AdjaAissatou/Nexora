package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.recherche.domain.entity.RechercheSauvegardee;
import sn.ucad.nexora.recherche.domain.repository.RechercheSauvegardeeRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.RechercheSauvegardeeJpaEntity;

import java.util.List;
import java.util.Optional;

@Component
public class RechercheSauvegardeeRepositoryAdapter implements RechercheSauvegardeeRepository {

    private final RechercheSauvegardeeJpaRepository jpa;

    public RechercheSauvegardeeRepositoryAdapter(RechercheSauvegardeeJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public RechercheSauvegardee save(RechercheSauvegardee rs) {
        return toDomain(jpa.save(toEntity(rs)));
    }

    @Override
    public Optional<RechercheSauvegardee> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<RechercheSauvegardee> findByUtilisateurId(Long uid) {
        return jpa.findByUtilisateurIdOrderByDateCreationDesc(uid).stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(Long id) { jpa.deleteById(id); }

    private RechercheSauvegardeeJpaEntity toEntity(RechercheSauvegardee rs) {
        RechercheSauvegardeeJpaEntity e = new RechercheSauvegardeeJpaEntity();
        e.setId(rs.getId()); e.setUtilisateurId(rs.getUtilisateurId());
        e.setNom(rs.getNom()); e.setMotCle(rs.getMotCle());
        e.setCategorie(rs.getCategorie()); e.setRayonKm(rs.getRayonKm());
        e.setLatitude(rs.getLatitude()); e.setLongitude(rs.getLongitude());
        e.setActive(rs.isActive()); e.setDateCreation(rs.getDateCreation());
        return e;
    }

    private RechercheSauvegardee toDomain(RechercheSauvegardeeJpaEntity e) {
        RechercheSauvegardee rs = new RechercheSauvegardee();
        rs.setId(e.getId()); rs.setUtilisateurId(e.getUtilisateurId());
        rs.setNom(e.getNom()); rs.setMotCle(e.getMotCle());
        rs.setCategorie(e.getCategorie()); rs.setRayonKm(e.getRayonKm());
        rs.setLatitude(e.getLatitude()); rs.setLongitude(e.getLongitude());
        rs.setActive(Boolean.TRUE.equals(e.getActive()));
        rs.setDateCreation(e.getDateCreation());
        return rs;
    }
}
