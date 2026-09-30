package sn.ucad.nexora.administration.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.ActionJournalResponse;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.ChiffresPublicsResponse;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.PageJournalResponse;
import sn.ucad.nexora.administration.application.dto.AdministrationDtos.TableauDeBordResponse;
import sn.ucad.nexora.administration.infrastructure.persistence.AdministrationLectureRepository;

/** Tableau de bord et journal du back-office (docs/architecture-acteurs.md §9.4). */
@Service
public class AdministrationService {

    static final int TAILLE_PAGE_JOURNAL = 50;
    private static final int DERNIERES_ACTIONS = 8;

    private final AdministrationLectureRepository lecture;

    public AdministrationService(AdministrationLectureRepository lecture) {
        this.lecture = lecture;
    }

    @Transactional(readOnly = true)
    public TableauDeBordResponse tableauDeBord(boolean avecJournal) {
        return new TableauDeBordResponse(
                lecture.compter("SELECT COUNT(*) FROM accounts"),
                lecture.compter("SELECT COUNT(*) FROM accounts WHERE locked OR NOT enabled"),
                lecture.compter("SELECT COUNT(*) FROM accounts WHERE created_at > NOW() - INTERVAL '30 days'"),
                lecture.compter("SELECT COUNT(*) FROM espace_professionnel"),
                lecture.compter("SELECT COUNT(*) FROM espace_professionnel WHERE statut = 'ACTIF'"),
                lecture.compter("SELECT COUNT(*) FROM espace_professionnel WHERE statut = 'SUSPENDU'"),
                lecture.compter("SELECT COUNT(*) FROM espace_professionnel WHERE verifie"),
                lecture.compter("SELECT COUNT(*) FROM offre"),
                lecture.compter("SELECT COUNT(*) FROM offre WHERE statut = 'PUBLIE'"),
                lecture.compter("SELECT COUNT(*) FROM offre WHERE statut = 'SUSPENDU'"),
                lecture.compter("SELECT COUNT(*) FROM verification_espace WHERE statut = 'EN_ATTENTE'"),
                lecture.compter("SELECT COUNT(*) FROM verification_espace WHERE statut IN ('EN_COURS', 'A_COMPLETER')"),
                lecture.compter("SELECT COUNT(*) FROM signalement WHERE statut = 'EN_ATTENTE'"),
                lecture.compter("SELECT COUNT(*) FROM avis"),
                avecJournal ? lecture.journal(null, null, DERNIERES_ACTIONS, 0) : List.of());
    }

    /** Ce que la vitrine publique montre : uniquement ce qui est visible (espaces actifs, offres publiées). */
    @Transactional(readOnly = true)
    public ChiffresPublicsResponse chiffresPublics() {
        return new ChiffresPublicsResponse(
                lecture.compter("SELECT COUNT(*) FROM espace_professionnel WHERE statut = 'ACTIF'"),
                lecture.compter("""
                        SELECT COUNT(*) FROM offre o JOIN espace_professionnel e ON e.id_espace = o.id_espace
                        WHERE o.statut = 'PUBLIE' AND e.statut = 'ACTIF'"""),
                lecture.compter("SELECT COUNT(*) FROM espace_professionnel WHERE statut = 'ACTIF' AND verifie"),
                lecture.compter("""
                        SELECT COUNT(DISTINCT LOWER(a.commune)) FROM adresse a
                        JOIN espace_professionnel e ON e.id_espace = a.id_espace
                        WHERE e.statut = 'ACTIF' AND a.commune IS NOT NULL"""));
    }

    @Transactional(readOnly = true)
    public PageJournalResponse journal(String module, String recherche, int page) {
        int p = Math.max(page, 0);
        // Une ligne de plus que la page pour savoir s'il existe une page suivante.
        List<ActionJournalResponse> lignes = lecture.journal(nettoyer(module), nettoyer(recherche),
                TAILLE_PAGE_JOURNAL + 1, p * TAILLE_PAGE_JOURNAL);
        boolean suivante = lignes.size() > TAILLE_PAGE_JOURNAL;
        return new PageJournalResponse(suivante ? lignes.subList(0, TAILLE_PAGE_JOURNAL) : lignes, p, suivante,
                lecture.modulesJournal());
    }

    private static String nettoyer(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
