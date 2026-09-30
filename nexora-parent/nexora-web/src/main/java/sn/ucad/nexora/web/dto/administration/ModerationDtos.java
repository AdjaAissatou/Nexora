package sn.ucad.nexora.web.dto.administration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Miroirs des réponses de modération : {@code ModerationEspaceDtos} (espace-service) et
 * {@code ModerationOffreDtos} (catalogue-service), docs/architecture-acteurs.md §9.9.
 */
public final class ModerationDtos {

    private ModerationDtos() {}

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

    public record OffreModeree(Long id, String titre, BigDecimal prix, String statut, String categorie,
                               Long espaceId, String espaceNom, String espaceStatut, Long proprietaireId,
                               String proprietaireNom, LocalDateTime dateCreation, String motifModeration,
                               LocalDateTime dateModeration, String moderateur) {}

    public record PageOffres(List<OffreModeree> offres, int page, boolean pageSuivante, long total) {}

    public record MotifRequest(String motif) {}
}
