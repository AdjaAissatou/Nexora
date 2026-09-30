package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.recherche.domain.entity.Avis;
import sn.ucad.nexora.recherche.domain.repository.AvisRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.AvisJpaEntity;

import java.util.List;
import java.util.Optional;

@Component
public class AvisRepositoryAdapter implements AvisRepository {

    private final AvisJpaRepository jpa;

    public AvisRepositoryAdapter(AvisJpaRepository jpa) { this.jpa = jpa; }

    @Override public Avis save(Avis a) { return toDomain(jpa.saveAndFlush(toEntity(a))); }
    @Override public Optional<Avis> findById(Long id) { return jpa.findById(id).map(this::toDomain); }

    @Override
    public List<Avis> findByOffreId(Long offreId) {
        return jpa.findByOffreIdOrderByDateCreationDesc(offreId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Avis> findByEspaceId(Long espaceId) {
        return jpa.findByEspaceIdOrderByDateCreationDesc(espaceId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Avis> findByUtilisateurId(Long uid) {
        return jpa.findByUtilisateurIdOrderByDateCreationDesc(uid).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByUtilisateurIdAndOffreId(Long uid, Long oid) {
        return jpa.existsByUtilisateurIdAndOffreId(uid, oid);
    }

    @Override public void delete(Long id) { jpa.deleteById(id); }
    @Override public void flush() { jpa.flush(); }

    private AvisJpaEntity toEntity(Avis a) {
        AvisJpaEntity e = new AvisJpaEntity();
        e.setId(a.getId()); e.setUtilisateurId(a.getUtilisateurId());
        e.setOffreId(a.getOffreId()); e.setEspaceId(a.getEspaceId());
        e.setNote(a.getNote()); e.setCommentaire(a.getCommentaire());
        e.setReponseFournisseur(a.getReponseFournisseur());
        e.setDateCreation(a.getDateCreation()); e.setDateReponse(a.getDateReponse());
        return e;
    }

    private Avis toDomain(AvisJpaEntity e) {
        Avis a = new Avis();
        a.setId(e.getId()); a.setUtilisateurId(e.getUtilisateurId());
        a.setOffreId(e.getOffreId()); a.setEspaceId(e.getEspaceId());
        a.setNote(e.getNote() != null ? e.getNote() : 0);
        a.setCommentaire(e.getCommentaire());
        a.setReponseFournisseur(e.getReponseFournisseur());
        a.setDateCreation(e.getDateCreation()); a.setDateReponse(e.getDateReponse());
        return a;
    }
}
