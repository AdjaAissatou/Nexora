package sn.ucad.nexora.recherche.application.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.notification.Notifications;
import sn.ucad.nexora.recherche.application.dto.request.PublierAvisRequest;
import sn.ucad.nexora.recherche.application.dto.response.AvisDtos;
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
    private final Notifications notifications;

    public AvisService(AvisRepository repository, AvisModerationRepository lecture, Notifications notifications) {
        this.repository = repository;
        this.lecture = lecture;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public AvisResponse publier(Long utilisateurId, PublierAvisRequest request) {
        if ((request.getOffreId() == null) == (request.getEspaceId() == null)) {
            throw new BusinessException("Un avis porte sur un espace ou sur une offre");
        }
        String commentaire = texte(request.getCommentaire(), "Commentaire");
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
        // Le professionnel est prévenu, avec un lien direct vers ses avis reçus pour répondre.
        notifications.envoyer(cible.proprietaireId(), Notifications.Type.INFO,
                "Nouvel avis " + etoiles(request.getNote()) + " sur « " + cible.libelle() + " »",
                commentaire == null ? "Un client a noté « " + cible.libelle() + " » sans commentaire."
                        : "« " + extrait(commentaire) + " » — Répondez-lui depuis Mon espace.",
                "/mon-espace.xhtml?id=" + cible.espaceId() + "#avis");
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

    // ------------------------------------------------------------------ §22 : modifier, mes avis, avis reçus, réponse

    @Override
    @Transactional
    public AvisResponse modifier(Long utilisateurId, Long avisId, AvisDtos.ModifierAvisRequest request) {
        AvisModerationRepository.EtatAvis a = etat(avisId);
        if (!a.auteurId().equals(utilisateurId)) throw new BusinessException("Action non autorisée");
        if (a.masque()) {
            throw new BusinessException("Votre avis a été masqué par la modération : il ne peut plus être modifié");
        }
        if (request.note() == null || request.note() < 1 || request.note() > 5) {
            throw new BusinessException("Choisissez une note de 1 à 5 étoiles");
        }
        lecture.modifier(avisId, request.note(), texte(request.commentaire(), "Commentaire"));
        lecture.recalculerNote(a.espaceId());
        return lecture.visible(avisId).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisDtos.MonAvisDetail> mesAvis(Long utilisateurId) {
        return lecture.mesAvis(utilisateurId);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisDtos.AvisRecus avisRecus(Long utilisateurId, Long espaceId) {
        AvisModerationRepository.Cible espace = lecture.cibleEspace(espaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Espace introuvable"));
        if (!utilisateurId.equals(espace.proprietaireId())) throw new BusinessException("Action non autorisée");
        List<AvisDtos.AvisRecu> avis = lecture.avisRecus(espaceId);
        Integer[] repartition = {0, 0, 0, 0, 0};
        avis.forEach(a -> repartition[a.note() - 1]++);
        double moyenne = avis.isEmpty() ? 0
                : Math.round(avis.stream().mapToInt(AvisDtos.AvisRecu::note).average().orElse(0) * 10) / 10.0;
        int sansReponse = (int) avis.stream().filter(a -> a.reponse() == null || a.reponse().isBlank()).count();
        return new AvisDtos.AvisRecus(espaceId, espace.libelle(), moyenne, avis.size(), List.of(repartition), sansReponse,
                lecture.avisMasques(espaceId), avis);
    }

    @Override
    @Transactional
    public AvisDtos.AvisRecu repondre(Long utilisateurId, Long avisId, String texte) {
        AvisModerationRepository.EtatAvis a = etatPourProprietaire(utilisateurId, avisId);
        String reponse = texte(texte, "Réponse");
        if (reponse == null) throw new BusinessException("Écrivez votre réponse");
        lecture.definirReponse(avisId, reponse);
        // L'auteur est prévenu de la première réponse seulement (pas à chaque retouche).
        if (a.reponse() == null || a.reponse().isBlank()) {
            notifications.envoyer(a.auteurId(), Notifications.Type.INFO, "« " + a.espaceNom() + " » a répondu à votre avis",
                    "« " + extrait(reponse) + " »",
                    a.offreId() != null ? "/offre.xhtml?id=" + a.offreId() : "/espace.xhtml?id=" + a.espaceId() + "#avis");
        }
        return recu(avisId, a.espaceId());
    }

    @Override
    @Transactional
    public AvisDtos.AvisRecu retirerReponse(Long utilisateurId, Long avisId) {
        AvisModerationRepository.EtatAvis a = etatPourProprietaire(utilisateurId, avisId);
        lecture.definirReponse(avisId, null);
        return recu(avisId, a.espaceId());
    }

    private AvisModerationRepository.EtatAvis etat(Long avisId) {
        return lecture.etat(avisId).orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
    }

    /** Seul le propriétaire de l'espace répond, et pas à un avis masqué par la modération. */
    private AvisModerationRepository.EtatAvis etatPourProprietaire(Long utilisateurId, Long avisId) {
        AvisModerationRepository.EtatAvis a = etat(avisId);
        if (!a.proprietaireId().equals(utilisateurId)) {
            throw new BusinessException("Seul le propriétaire de l'espace peut répondre à cet avis");
        }
        if (a.masque()) throw new BusinessException("Cet avis a été masqué par la modération : on n'y répond plus");
        return a;
    }

    private AvisDtos.AvisRecu recu(Long avisId, Long espaceId) {
        return lecture.avisRecus(espaceId).stream().filter(r -> r.id().equals(avisId)).findFirst().orElseThrow();
    }

    /** Texte facultatif nettoyé ; trop long : refusé (paramètre AVIS_LONGUEUR_MAX). */
    private String texte(String brut, String quoi) {
        String t = brut == null || brut.isBlank() ? null : brut.trim();
        int max = parametres == null ? COMMENTAIRE_MAX : parametres.entier("AVIS_LONGUEUR_MAX", COMMENTAIRE_MAX);
        if (t != null && t.length() > max) throw new BusinessException(quoi + " trop long (" + max + " caractères au plus)");
        return t;
    }

    static String etoiles(int note) {
        int n = Math.max(0, Math.min(5, note));
        return "★".repeat(n) + "☆".repeat(5 - n);
    }

    static String extrait(String texte) {
        return texte.length() <= 140 ? texte : texte.substring(0, 137).trim() + "…";
    }
}
