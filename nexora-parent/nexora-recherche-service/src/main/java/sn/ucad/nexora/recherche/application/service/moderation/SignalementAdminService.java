package sn.ucad.nexora.recherche.application.service.moderation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.notification.Notifications;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.PageSignalements;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.SignalementDetail;
import sn.ucad.nexora.recherche.application.dto.response.moderation.ModerationRechercheDtos.SignalementResume;
import sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository;
import sn.ucad.nexora.recherche.infrastructure.persistence.moderation.AvisModerationRepository.Cible;
import sn.ucad.nexora.recherche.infrastructure.persistence.moderation.SignalementAdminRepository;

/**
 * Signalements (docs/architecture-acteurs.md §9.10) : dépôt par un utilisateur connecté, puis
 * file de traitement de la modération (permission GERER_SIGNALEMENTS) : prendre en charge, traiter
 * ou rejeter, toujours avec un commentaire. La sanction elle-même (suspendre, masquer) passe par la
 * modération des espaces, des offres ou des avis : le signalement en garde la conclusion.
 */
@Service
public class SignalementAdminService {

    static final int TAILLE_PAGE = 25;
    private static final String MODULE = "SIGNALEMENTS";
    private static final int DESCRIPTION_MAX = 1000;

    /** Motifs proposés à l'utilisateur ; AUTRE exige une description. */
    public static final Map<String, String> MOTIFS = Map.of(
            "ARNAQUE", "Arnaque ou fraude",
            "INFORMATIONS_FAUSSES", "Informations fausses ou trompeuses",
            "CONTENU_INAPPROPRIE", "Contenu choquant ou inapproprié",
            "LIEU_INEXISTANT", "Le lieu n'existe pas ou est fermé",
            "AVIS_FAUX", "Avis faux ou malveillant",
            "AUTRE", "Autre");

    private static final Set<String> STATUTS = Set.of("EN_ATTENTE", "EN_COURS", "TRAITE", "REJETE");
    private static final Set<String> TYPES = Set.of("ESPACE", "OFFRE", "AVIS");

    private final SignalementAdminRepository signalements;
    private final AvisModerationRepository avis;
    private final JournalActions journal;
    private final Notifications notifications;

    public SignalementAdminService(SignalementAdminRepository signalements, AvisModerationRepository avis,
                                   JournalActions journal, Notifications notifications) {
        this.signalements = signalements;
        this.avis = avis;
        this.journal = journal;
        this.notifications = notifications;
    }

    // ------------------------------------------------------------------ dépôt (utilisateur)

    @Transactional
    public void signaler(Long utilisateurId, Long espaceId, Long offreId, Long avisId, String motif, String description) {
        int cibles = (espaceId != null ? 1 : 0) + (offreId != null ? 1 : 0) + (avisId != null ? 1 : 0);
        if (cibles != 1) throw new BusinessException("Un signalement vise exactement un espace, une offre ou un avis");
        if (motif == null || !MOTIFS.containsKey(motif)) throw new BusinessException("Motif de signalement inconnu");
        String d = AvisModerationService.vide(description);
        if ("AUTRE".equals(motif) && d == null) throw new BusinessException("Précisez ce qui ne va pas");
        if (d != null && d.length() > DESCRIPTION_MAX) throw new BusinessException("Description trop longue (" + DESCRIPTION_MAX + " caractères au plus)");
        Cible c = (espaceId != null ? avis.cibleEspace(espaceId) : offreId != null ? avis.cibleOffre(offreId) : avis.cibleAvis(avisId))
                .filter(Cible::visible)
                .orElseThrow(() -> new ResourceNotFoundException("Élément introuvable"));
        if (utilisateurId.equals(c.proprietaireId())) throw new BusinessException("Vous ne pouvez pas signaler votre propre contenu");
        if (signalements.dejaOuvert(utilisateurId, espaceId, offreId, avisId)) {
            throw new BusinessException("Vous avez déjà signalé cet élément : la modération l'examine");
        }
        signalements.creer(utilisateurId, espaceId, offreId, avisId, motif, d);
    }

    // ------------------------------------------------------------------ file de traitement

    @Transactional(readOnly = true)
    public PageSignalements rechercher(String statut, String type, int page) {
        String s = AvisModerationService.vide(statut);
        String t = AvisModerationService.vide(type);
        if (s != null && !STATUTS.contains(s)) throw new BusinessException("Statut de signalement inconnu : " + s);
        if (t != null && !TYPES.contains(t)) throw new BusinessException("Type de signalement inconnu : " + t);
        int p = Math.max(page, 0);
        List<SignalementResume> lignes = signalements.rechercher(s, t, TAILLE_PAGE + 1, p * TAILLE_PAGE);
        boolean suivante = lignes.size() > TAILLE_PAGE;
        return new PageSignalements(suivante ? lignes.subList(0, TAILLE_PAGE) : lignes, p, suivante, signalements.compter(s, t));
    }

    @Transactional(readOnly = true)
    public SignalementDetail detail(Long id) {
        SignalementResume s = signalement(id);
        return new SignalementDetail(s, "AVIS".equals(s.type()) ? avis.avis(s.cibleId()).orElse(null) : null);
    }

    @Transactional
    public SignalementDetail prendreEnCharge(UUID auteur, Long id) {
        SignalementResume s = signalement(id);
        Long moi = exigerNeutre(auteur, s);
        if (!"EN_ATTENTE".equals(s.statut())) throw new BusinessException("Ce signalement n'est plus en attente");
        signalements.definirStatut(id, "EN_COURS", moi, null, false);
        journal.enregistrer(auteur, MODULE, "PRENDRE_SIGNALEMENT", "signalement", id,
                "Signalement n° " + id + " (" + s.type().toLowerCase() + " « " + s.cibleLibelle() + " ») pris en charge");
        return detail(id);
    }

    /** Clôt le signalement : TRAITE (une suite a été donnée) ou REJETE (rien de contraire aux règles). */
    @Transactional
    public SignalementDetail clore(UUID auteur, Long id, boolean fonde, String commentaire) {
        AvisModerationService.exigerMotif(commentaire);
        SignalementResume s = signalement(id);
        Long moi = exigerNeutre(auteur, s);
        if (!"EN_ATTENTE".equals(s.statut()) && !"EN_COURS".equals(s.statut())) {
            throw new BusinessException("Ce signalement est déjà clos");
        }
        String c = commentaire.trim();
        signalements.definirStatut(id, fonde ? "TRAITE" : "REJETE", moi, c, true);
        journal.enregistrer(auteur, MODULE, fonde ? "TRAITER_SIGNALEMENT" : "REJETER_SIGNALEMENT", "signalement", id,
                "Signalement n° " + id + " (" + s.type().toLowerCase() + " « " + s.cibleLibelle() + " ») "
                        + (fonde ? "traité" : "rejeté") + " : " + c);
        // Le signaleur apprend l'issue, sans le détail de la sanction ni l'identité du modérateur.
        notifications.envoyer(s.signaleurId(), Notifications.Type.INFO, "Votre signalement a été examiné",
                fonde ? "Merci : votre signalement concernant « " + s.cibleLibelle() + " » a été traité par la modération de Nexora."
                      : "Votre signalement concernant « " + s.cibleLibelle() + " » a été examiné : la modération n'a pas relevé "
                        + "de manquement aux règles de Nexora.", null);
        return detail(id);
    }

    private SignalementResume signalement(Long id) {
        return signalements.signalement(id).orElseThrow(() -> new ResourceNotFoundException("Signalement introuvable"));
    }

    /** On ne traite pas un signalement qui vise son propre contenu. */
    private Long exigerNeutre(UUID auteur, SignalementResume s) {
        Long moi = avis.utilisateurId(auteur).orElseThrow(() -> new BusinessException("Profil utilisateur introuvable"));
        Long proprietaire = s.espaceId() == null ? null : avis.proprietaireEspace(s.espaceId()).orElse(null);
        boolean auteurAvis = "AVIS".equals(s.type()) && avis.cibleAvis(s.cibleId()).map(c -> moi.equals(c.proprietaireId())).orElse(false);
        if (moi.equals(proprietaire) || auteurAvis) {
            throw new BusinessException("Ce signalement vise votre propre contenu : un autre modérateur doit le traiter");
        }
        return moi;
    }
}
