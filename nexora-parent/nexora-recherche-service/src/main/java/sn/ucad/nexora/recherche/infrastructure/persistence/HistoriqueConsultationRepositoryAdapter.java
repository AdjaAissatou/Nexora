package sn.ucad.nexora.recherche.infrastructure.persistence;

import org.springframework.stereotype.Component;
import sn.ucad.nexora.recherche.domain.entity.HistoriqueConsultation;
import sn.ucad.nexora.recherche.domain.repository.HistoriqueConsultationRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.entity.HistoriqueConsultationJpaEntity;

import java.util.List;

@Component
public class HistoriqueConsultationRepositoryAdapter implements HistoriqueConsultationRepository {

    private final HistoriqueConsultationJpaRepository jpa;

    public HistoriqueConsultationRepositoryAdapter(HistoriqueConsultationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public HistoriqueConsultation save(HistoriqueConsultation c) {
        return toDomain(jpa.save(toEntity(c)));
    }

    @Override
    public List<HistoriqueConsultation> findByUtilisateurId(Long utilisateurId) {
        return jpa.findByUtilisateurIdOrderByDateConsultationDesc(utilisateurId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteByUtilisateurId(Long utilisateurId) {
        jpa.deleteByUtilisateurId(utilisateurId);
    }

    private HistoriqueConsultationJpaEntity toEntity(HistoriqueConsultation c) {
        HistoriqueConsultationJpaEntity e = new HistoriqueConsultationJpaEntity();
        e.setId(c.getId());
        e.setUtilisateurId(c.getUtilisateurId());
        e.setOffreId(c.getOffreId());
        e.setEspaceId(c.getEspaceId());
        e.setDureeSecondes(c.getDureeSecondes());
        e.setDateConsultation(c.getDateConsultation());
        return e;
    }

    private HistoriqueConsultation toDomain(HistoriqueConsultationJpaEntity e) {
        HistoriqueConsultation c = new HistoriqueConsultation();
        c.setId(e.getId());
        c.setUtilisateurId(e.getUtilisateurId());
        c.setOffreId(e.getOffreId());
        c.setEspaceId(e.getEspaceId());
        c.setDureeSecondes(e.getDureeSecondes());
        c.setDateConsultation(e.getDateConsultation());
        return c;
    }
}
