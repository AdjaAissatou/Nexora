package sn.ucad.nexora.espace.application.dto.response.moderation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Réponses de la modération des espaces (docs/architecture-acteurs.md §9.9). */
public final class ModerationEspaceDtos {

    private ModerationEspaceDtos() {}

    /** Une ligne de la liste. {@code offresPubliees} : offres visibles si l'espace est actif. */
    public record EspaceModere(Long id, String nom, String typeEspace, String commune, String statut, boolean verifie,
                               boolean certifie, Long proprietaireId, UUID proprietaireCompte, String proprietaireNom,
                               String proprietaireEmail, long offresPubliees, long offresSuspendues,
                               LocalDateTime dateCreation, String motifModeration, LocalDateTime dateModeration,
                               String moderateur) {}

    public record PageEspaces(List<EspaceModere> espaces, int page, boolean pageSuivante, long total) {}

    public record OffreDeLEspace(Long id, String titre, BigDecimal prix, String statut, String motifModeration,
                                 LocalDateTime dateModeration) {}

    public record ActionHistorique(LocalDateTime date, String auteur, String action, String description) {}

    public record FicheEspace(EspaceModere espace, String description, String telephone, String email,
                              String adresse, List<OffreDeLEspace> offres, List<ActionHistorique> historique) {}

    /** Ce que le professionnel voit de la modération de son espace. */
    public record ModerationVisible(String statut, String motif, LocalDateTime date) {}
}
