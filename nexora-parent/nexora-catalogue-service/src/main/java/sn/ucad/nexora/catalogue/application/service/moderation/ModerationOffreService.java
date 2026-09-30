package sn.ucad.nexora.catalogue.application.service.moderation;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.catalogue.application.dto.response.moderation.ModerationOffreDtos.OffreModeree;
import sn.ucad.nexora.catalogue.application.dto.response.moderation.ModerationOffreDtos.PageOffres;
import sn.ucad.nexora.catalogue.infrastructure.persistence.moderation.ModerationOffreRepository;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.notification.Notifications;

/**
 * Modération des offres (docs/architecture-acteurs.md §9.9). La permission MODERER_OFFRES est
 * vérifiée par SecurityConfig ; ici : motif obligatoire, pas de modération de ses propres offres,
 * transitions PUBLIE ⇄ SUSPENDU seulement. Journalisé et notifié dans la même transaction.
 */
@Service
public class ModerationOffreService {

    static final int TAILLE_PAGE = 25;
    private static final String MODULE = "MODERATION";
    private static final Set<String> STATUTS = Set.of("BROUILLON", "PUBLIE", "SUSPENDU", "EXPIRE");
    private static final String URL_PRO = "/mon-espace.xhtml";

    private final ModerationOffreRepository moderation;
    private final JournalActions journal;
    private final Notifications notifications;

    public ModerationOffreService(ModerationOffreRepository moderation, JournalActions journal, Notifications notifications) {
        this.moderation = moderation;
        this.journal = journal;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public PageOffres rechercher(String recherche, String statut, Long espaceId, int page) {
        String r = recherche == null || recherche.isBlank() ? null : recherche.trim();
        String s = statut == null || statut.isBlank() ? null : statut.trim();
        if (s != null && !STATUTS.contains(s)) throw new BusinessException("Statut d'offre inconnu : " + s);
        int p = Math.max(page, 0);
        List<OffreModeree> lignes = moderation.rechercher(r, s, espaceId, TAILLE_PAGE + 1, p * TAILLE_PAGE);
        boolean suivante = lignes.size() > TAILLE_PAGE;
        return new PageOffres(suivante ? lignes.subList(0, TAILLE_PAGE) : lignes, p, suivante, moderation.compter(r, s, espaceId));
    }

    /** Suspend une offre : elle disparaît de la recherche et sa fiche publique répond « introuvable ». */
    @Transactional
    public OffreModeree suspendre(UUID auteur, Long offreId, String motif) {
        exigerMotif(motif);
        OffreModeree o = offre(offreId);
        Long moderateur = exigerAutreQueProprietaire(auteur, o);
        if ("SUSPENDU".equals(o.statut())) throw new BusinessException("Cette offre est déjà suspendue");
        if (!"PUBLIE".equals(o.statut())) throw new BusinessException("Seule une offre publiée peut être suspendue (statut : " + o.statut() + ")");
        String m = motif.trim();
        moderation.definirStatut(offreId, "SUSPENDU", m, moderateur);
        journal.enregistrer(auteur, MODULE, "SUSPENDRE_OFFRE", "offre", offreId,
                "Offre « " + o.titre() + " » (" + o.espaceNom() + ") suspendue : " + m);
        notifications.envoyer(o.proprietaireId(), Notifications.Type.AVERTISSEMENT, "Une offre est suspendue",
                "Votre offre « " + o.titre() + " » a été suspendue par la modération de Nexora : " + m
                        + ". Elle n'apparaît plus dans la recherche. Modifiez-la pour corriger ce qui est signalé : "
                        + "la modération pourra alors la republier.", URL_PRO);
        return offre(offreId);
    }

    @Transactional
    public OffreModeree republier(UUID auteur, Long offreId, String motif) {
        exigerMotif(motif);
        OffreModeree o = offre(offreId);
        Long moderateur = exigerAutreQueProprietaire(auteur, o);
        if (!"SUSPENDU".equals(o.statut())) throw new BusinessException("Cette offre n'est pas suspendue");
        String m = motif.trim();
        moderation.definirStatut(offreId, "PUBLIE", null, moderateur);
        journal.enregistrer(auteur, MODULE, "REPUBLIER_OFFRE", "offre", offreId,
                "Offre « " + o.titre() + " » (" + o.espaceNom() + ") republiée : " + m);
        notifications.envoyer(o.proprietaireId(), Notifications.Type.SUCCES, "Votre offre est de nouveau visible",
                "Votre offre « " + o.titre() + " » a été republiée par la modération de Nexora : " + m + ".", URL_PRO);
        return offre(offreId);
    }

    private OffreModeree offre(Long offreId) {
        return moderation.offre(offreId).orElseThrow(() -> new ResourceNotFoundException("Offre introuvable"));
    }

    private Long exigerAutreQueProprietaire(UUID auteur, OffreModeree o) {
        Long moderateur = moderation.utilisateurId(auteur).orElseThrow(() -> new BusinessException("Profil utilisateur introuvable"));
        if (moderateur.equals(o.proprietaireId())) throw new BusinessException("Vous ne pouvez pas modérer vos propres offres");
        return moderateur;
    }

    private static void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
    }
}
