package sn.ucad.nexora.espace.application.service.moderation;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.notification.Notifications;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.EspaceModere;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.FicheEspace;
import sn.ucad.nexora.espace.application.dto.response.moderation.ModerationEspaceDtos.PageEspaces;
import sn.ucad.nexora.espace.infrastructure.persistence.moderation.ModerationEspaceRepository;

/**
 * Modération des espaces (docs/architecture-acteurs.md §9.9). La permission MODERER_ESPACES est
 * vérifiée par SecurityConfig ; ce service applique les règles : motif obligatoire, pas de
 * modération de son propre espace, transitions ACTIF ⇄ SUSPENDU seulement. Chaque décision est
 * journalisée et notifiée au professionnel dans la même transaction.
 */
@Service
public class ModerationEspaceService {

    static final int TAILLE_PAGE = 25;
    static final String MODULE = "MODERATION";
    private static final Set<String> STATUTS = Set.of("BROUILLON", "EN_ATTENTE", "EN_VERIFICATION", "ACTIF", "SUSPENDU", "REFUSE", "FERME");
    private static final String URL_PRO = "/mon-espace.xhtml";

    private final ModerationEspaceRepository moderation;
    private final JournalActions journal;
    private final Notifications notifications;

    public ModerationEspaceService(ModerationEspaceRepository moderation, JournalActions journal, Notifications notifications) {
        this.moderation = moderation;
        this.journal = journal;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public PageEspaces rechercher(String recherche, String statut, Boolean verifie, int page) {
        String r = nettoyer(recherche);
        String s = nettoyer(statut);
        if (s != null && !STATUTS.contains(s)) throw new BusinessException("Statut d'espace inconnu : " + s);
        int p = Math.max(page, 0);
        List<EspaceModere> lignes = moderation.rechercher(r, s, verifie, TAILLE_PAGE + 1, p * TAILLE_PAGE);
        boolean suivante = lignes.size() > TAILLE_PAGE;
        return new PageEspaces(suivante ? lignes.subList(0, TAILLE_PAGE) : lignes, p, suivante, moderation.compter(r, s, verifie));
    }

    @Transactional(readOnly = true)
    public FicheEspace fiche(Long espaceId) {
        EspaceModere e = espace(espaceId);
        Object[] d = moderation.details(espaceId);
        return new FicheEspace(e, (String) d[0], (String) d[1], (String) d[2], (String) d[3],
                moderation.offres(espaceId), moderation.historique(espaceId));
    }

    /**
     * Suspend un espace : il disparaît de la recherche, de l'accueil et sa fiche publique répond
     * « introuvable », offres comprises. Son propriétaire le voit toujours, avec le motif.
     */
    @Transactional
    public FicheEspace suspendre(UUID auteur, Long espaceId, String motif) {
        exigerMotif(motif);
        EspaceModere e = espace(espaceId);
        Long moderateur = exigerAutreQueProprietaire(auteur, e);
        if ("SUSPENDU".equals(e.statut())) throw new BusinessException("Cet espace est déjà suspendu");
        if (!"ACTIF".equals(e.statut())) throw new BusinessException("Seul un espace actif peut être suspendu (statut : " + e.statut() + ")");
        String m = motif.trim();
        moderation.definirStatut(espaceId, "SUSPENDU", m, moderateur);
        journal.enregistrer(auteur, MODULE, "SUSPENDRE_ESPACE", "espace", espaceId, "Espace « " + e.nom() + " » suspendu : " + m);
        notifications.envoyer(e.proprietaireId(), Notifications.Type.AVERTISSEMENT, "Votre espace est suspendu",
                "Votre espace « " + e.nom() + " » a été suspendu par la modération de Nexora : " + m
                        + ". Il n'apparaît plus dans la recherche. Corrigez ce qui est signalé : "
                        + "la modération pourra alors le réactiver.", URL_PRO);
        return fiche(espaceId);
    }

    @Transactional
    public FicheEspace reactiver(UUID auteur, Long espaceId, String motif) {
        exigerMotif(motif);
        EspaceModere e = espace(espaceId);
        Long moderateur = exigerAutreQueProprietaire(auteur, e);
        if (!"SUSPENDU".equals(e.statut())) throw new BusinessException("Cet espace n'est pas suspendu");
        String m = motif.trim();
        moderation.definirStatut(espaceId, "ACTIF", null, moderateur);
        journal.enregistrer(auteur, MODULE, "REACTIVER_ESPACE", "espace", espaceId, "Espace « " + e.nom() + " » réactivé : " + m);
        notifications.envoyer(e.proprietaireId(), Notifications.Type.SUCCES, "Votre espace est de nouveau visible",
                "Votre espace « " + e.nom() + " » a été réactivé par la modération de Nexora : " + m + ".", URL_PRO);
        return fiche(espaceId);
    }

    private EspaceModere espace(Long espaceId) {
        return moderation.espace(espaceId).orElseThrow(() -> new ResourceNotFoundException("Espace introuvable"));
    }

    /** Un modérateur ne statue jamais sur son propre espace. Renvoie l'utilisateur du modérateur. */
    private Long exigerAutreQueProprietaire(UUID auteur, EspaceModere e) {
        Long moderateur = moderation.utilisateurId(auteur).orElseThrow(() -> new BusinessException("Profil utilisateur introuvable"));
        if (moderateur.equals(e.proprietaireId())) {
            throw new BusinessException("Vous ne pouvez pas modérer votre propre espace");
        }
        return moderateur;
    }

    static void exigerMotif(String motif) {
        if (motif == null || motif.isBlank()) throw new BusinessException("Le motif est obligatoire");
    }

    private static String nettoyer(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
