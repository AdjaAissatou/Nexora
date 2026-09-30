package sn.ucad.nexora.recherche.application.dto.response.moderation;

import java.time.LocalDateTime;
import java.util.List;

/** Réponses de la modération des avis et des signalements (docs/architecture-acteurs.md §9.10). */
public final class ModerationRechercheDtos {

    private ModerationRechercheDtos() {}

    /** Un avis vu par la modération. {@code espaceId} : espace concerné (directement ou par l'offre). */
    public record AvisModere(Long id, int note, String commentaire, Long auteurId, String auteurNom, String auteurEmail,
                             Long espaceId, String espaceNom, Long offreId, String offreTitre, LocalDateTime dateCreation,
                             boolean masque, String motifModeration, LocalDateTime dateModeration, String moderateur,
                             long signalementsOuverts) {}

    public record PageAvis(List<AvisModere> avis, int page, boolean pageSuivante, long total) {}

    /**
     * Un signalement. {@code type} : ESPACE, OFFRE ou AVIS ; {@code cibleId} l'id visé ;
     * {@code cibleLibelle} son nom, son titre ou l'extrait de l'avis ; {@code espaceId} l'espace concerné
     * (pour les liens vers sa fiche de modération).
     */
    public record SignalementResume(Long id, String type, Long cibleId, String cibleLibelle, String cibleStatut,
                                    Long espaceId, String motif, String description, Long signaleurId, String signaleurNom,
                                    LocalDateTime dateCreation, String statut, String traitePar,
                                    LocalDateTime dateTraitement, String commentaire, long signalementsSurLaCible) {}

    public record PageSignalements(List<SignalementResume> signalements, int page, boolean pageSuivante, long total) {}

    /** Détail : le signalement et, pour un avis, l'avis lui-même. */
    public record SignalementDetail(SignalementResume signalement, AvisModere avis) {}

    public record ActionHistorique(LocalDateTime date, String auteur, String action, String description) {}
}
