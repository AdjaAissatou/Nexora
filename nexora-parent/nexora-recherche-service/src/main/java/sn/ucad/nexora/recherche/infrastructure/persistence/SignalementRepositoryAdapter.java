package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.recherche.domain.entity.Signalement;
import sn.ucad.nexora.recherche.domain.repository.SignalementRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.SignalementJpaEntity;

import java.util.List;

@Component
public class SignalementRepositoryAdapter implements SignalementRepository {

    private final SignalementJpaRepository jpa;

    public SignalementRepositoryAdapter(SignalementJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Signalement save(Signalement s) { return toDomain(jpa.save(toEntity(s))); }

    @Override
    public List<Signalement> findByUtilisateurId(Long uid) {
        return jpa.findByUtilisateurId(uid).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByUtilisateurIdAndOffreId(Long uid, Long oid) {
        return jpa.existsByUtilisateurIdAndOffreId(uid, oid);
    }

    private SignalementJpaEntity toEntity(Signalement s) {
        SignalementJpaEntity e = new SignalementJpaEntity();
        e.setId(s.getId()); e.setUtilisateurId(s.getUtilisateurId());
        e.setOffreId(s.getOffreId()); e.setEspaceId(s.getEspaceId());
        e.setMotif(s.getMotif()); e.setDescription(s.getDescription());
        e.setStatut(s.getStatut()); e.setDateCreation(s.getDateCreation());
        return e;
    }

    private Signalement toDomain(SignalementJpaEntity e) {
        Signalement s = new Signalement();
        s.setId(e.getId()); s.setUtilisateurId(e.getUtilisateurId());
        s.setOffreId(e.getOffreId()); s.setEspaceId(e.getEspaceId());
        s.setMotif(e.getMotif()); s.setDescription(e.getDescription());
        s.setStatut(e.getStatut()); s.setDateCreation(e.getDateCreation());
        return s;
    }
}
