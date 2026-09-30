package sn.ucad.nexora.recherche.application.service.moderation;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.notification.Notifications;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.AvisModere;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.PageAvis;
import sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository;

/**
 * Modération des avis (docs/architecture-acteurs.md §9.10) : masquer ou rétablir, avec un motif.
 * Un avis masqué disparaît de la fiche publique et de la note de l'espace, recalculée dans la même
 * transaction. La permission MODERER_AVIS est vérifiée par SecurityConfig.
 */
@Service
public class AvisModerationService {

    static final int TAILLE_PAGE = 25;
    private static final String MODULE = "AVIS";
    private static final Set<String> ETATS = Set.of("VISIBLE", "MASQUE");

    private final AvisModerationRepository avis;
    private final JournalActions journal;
    private final Notifications notifications;

    public AvisModerationService(AvisModerationRepository avis, JournalActions journal, Notifications notifications) {
        this.avis = avis;
        this.journal = journal;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public PageAvis rechercher(String recherche, String etat, Long espaceId, int page) {
        String r = vide(recherche);
        String e = vide(etat);
        if (e != null && !ETATS.contains(e)) throw new BusinessException("État d'avis inconnu : " + e);
        int p = Math.max(page, 0);
        List<AvisModere> lignes = avis.rechercher(r, e, espaceId, TAILLE_PAGE + 1, p * TAILLE_PAGE);
        boolean suivante = lignes.size() > TAILLE_PAGE;
        return new PageAvis(suivante ? lignes.subList(0, TAILLE_PAGE) : lignes, p, suivante, avis.compter(r, e, espaceId));
    }

    @Transactional
    public AvisModere masquer(UUID auteur, Long avisId, String motif) {
        exigerMotif(motif);
        AvisModere a = avis(avisId);
        Long moderateur = exigerNeutre(auteur, a);
        if (a.masque()) throw new BusinessException("Cet avis est déjà masqué");
        String m = motif.trim();
        avis.definirMasque(avisId, true, m, moderateur);
        avis.recalculerNote(a.espaceId());
        journal.enregistrer(auteur, MODULE, "MASQUER_AVIS", "avis", avisId,
                "Avis de " + a.auteurNom() + " sur « " + a.espaceNom() + " » masqué : " + m);
        notifications.envoyer(a.auteurId(), Notifications.Type.AVERTISSEMENT, "Votre avis a été masqué",
                "Votre avis sur « " + a.espaceNom() + " » a été masqué par la modération de Nexora : " + m
                        + ". Il n'est plus visible et ne compte plus dans la note de l'espace.", "/espace.xhtml?id=" + a.espaceId());
        return avis(avisId);
    }

    @Transactional
    public AvisModere retablir(UUID auteur, Long avisId, String motif) {
        exigerMotif(motif);
        AvisModere a = avis(avisId);
        Long moderateur = exigerNeutre(auteur, a);
        if (!a.masque()) throw new BusinessException("Cet avis n'est pas masqué");
        String m = motif.trim();
        avis.definirMasque(avisId, false, null, moderateur);
        avis.recalculerNote(a.espaceId());
        journal.enregistrer(auteur, MODULE, "RETABLIR_AVIS", "avis", avisId,
                "Avis de " + a.auteurNom() + " sur « " + a.espaceNom() + " » rétabli : " + m);
        notifications.envoyer(a.auteurId(), Notifications.Type.SUCCES, "Votre avis est de nouveau visible",
                "Votre avis sur « " + a.espaceNom() + " » a été rétabli par la modération de Nexora : " + m + ".",
                "/espace.xhtml?id=" + a.espaceId());
        return avis(avisId);
    }

    private AvisModere avis(Long id) {
        return avis.avis(id).orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
    }

    /** Ni son propre avis, ni un avis sur son propre espace. Renvoie l'utilisateur du modérateur. */
    private Long exigerNeutre(UUID auteur, AvisModere a) {
        Long moi = avis.utilisateurId(auteur).orElseThrow(() -> new BusinessException("Profil utilisateur introuvable"));
        if (moi.equals(a.auteurId())) throw new BusinessException("Vous ne pouvez pas modérer votre propre avis");
        if (a.espaceId() != null && avis.proprietaireEspace(a.espaceId()).map(moi::equals).orElse(false)) {
            throw new BusinessException("Vous ne pouvez pas modérer un avis sur votre propre espace");
        }
        return moi;
    }

    static void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
    }

    static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
