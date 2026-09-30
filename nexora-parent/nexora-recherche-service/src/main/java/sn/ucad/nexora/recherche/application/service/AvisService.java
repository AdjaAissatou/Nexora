package sn.ucad.nexora.recherche.application.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.recherche.application.dto.request.PublierAvisRequest;
import sn.ucad.nexora.recherche.application.dto.response.AvisResponse;
import sn.ucad.nexora.recherche.application.usecase.AvisUseCase;
import sn.ucad.nexora.recherche.domain.entity.Avis;
import sn.ucad.nexora.recherche.domain.repository.AvisRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository.Cible;

/**
 * Avis publics (docs/architecture-acteurs.md §9.10). Seuls les avis non masqués sont lus ; la note
 * d'un espace est recalculée à chaque avis publié ou supprimé.
 */
@Service
public class AvisService implements AvisUseCase {

    /** Défaut du paramètre AVIS_LONGUEUR_MAX (§9.12). */
    private static final int COMMENTAIRE_MAX = 1000;

    @org.springframework.beans.factory.annotation.Autowired
    private sn.ucad.nexora.common.parametre.Parametres parametres;

    private final AvisRepository repository;
    private final AvisModerationRepository lecture;

    public AvisService(AvisRepository repository, AvisModerationRepository lecture) {
        this.repository = repository;
        this.lecture = lecture;
    }

    @Override
    @Transactional
    public AvisResponse publier(Long utilisateurId, PublierAvisRequest request) {
        if ((request.getOffreId() == null) == (request.getEspaceId() == null)) {
            throw new BusinessException("Un avis porte sur un espace ou sur une offre");
        }
        String commentaire = request.getCommentaire() == null || request.getCommentaire().isBlank() ? null : request.getCommentaire().trim();
        int max = parametres == null ? COMMENTAIRE_MAX : parametres.entier("AVIS_LONGUEUR_MAX", COMMENTAIRE_MAX);
        if (commentaire != null && commentaire.length() > max) {
            throw new BusinessException("Commentaire trop long (" + max + " caractères au plus)");
        }
        Cible cible = (request.getEspaceId() != null ? lecture.cibleEspace(request.getEspaceId()) : lecture.cibleOffre(request.getOffreId()))
                .filter(Cible::visible)
                .orElseThrow(() -> new ResourceNotFoundException("Élément introuvable"));
        if (utilisateurId.equals(cible.proprietaireId())) {
            throw new BusinessException("Vous ne pouvez pas donner un avis sur votre propre espace");
        }
        if (request.getOffreId() != null && repository.existsByUtilisateurIdAndOffreId(utilisateurId, request.getOffreId())) {
            throw new BusinessException("Vous avez déjà publié un avis sur cette offre");
        }
        if (request.getEspaceId() != null && lecture.aDejaNoteEspace(utilisateurId, request.getEspaceId())) {
            throw new BusinessException("Vous avez déjà publié un avis sur cet espace");
        }

        Avis a = new Avis();
        a.setUtilisateurId(utilisateurId);
        a.setOffreId(request.getOffreId());
        a.setEspaceId(request.getEspaceId());
        a.setNote(request.getNote());
        a.setCommentaire(commentaire);
        a.setDateCreation(LocalDateTime.now());
        Avis enregistre = repository.save(a);
        lecture.recalculerNote(cible.espaceId());
        return lecture.visible(enregistre.getId()).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisResponse> listerParOffre(Long offreId) {
        return lecture.visiblesParOffre(offreId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisResponse> listerParEspace(Long espaceId) {
        return lecture.visiblesParEspace(espaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisResponse visible(Long avisId) {
        return lecture.visible(avisId).orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
    }

    @Override
    @Transactional
    public void supprimer(Long utilisateurId, Long avisId) {
        Avis a = repository.findById(avisId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        if (!a.getUtilisateurId().equals(utilisateurId)) {
            throw new BusinessException("Action non autorisée");
        }
        Long espace = lecture.cibleAvis(avisId).map(Cible::espaceId).orElse(null);
        repository.delete(avisId);
        repository.flush();
        lecture.recalculerNote(espace);
    }
}
