package sn.ucad.nexora.catalogue.application.dto.response.moderation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Réponses de la modération des offres (docs/architecture-acteurs.md §9.9). */
public final class ModerationOffreDtos {

    private ModerationOffreDtos() {}

    public record OffreModeree(Long id, String titre, BigDecimal prix, String statut, String categorie,
                               Long espaceId, String espaceNom, String espaceStatut, Long proprietaireId,
                               String proprietaireNom, LocalDateTime dateCreation, String motifModeration,
                               LocalDateTime dateModeration, String moderateur) {}

    public record PageOffres(List<OffreModeree> offres, int page, boolean pageSuivante, long total) {}

    /** Statuts de l'offre et de son espace, et propriétaire : pour décider qui peut voir une offre non publique. */
    public record Visibilite(String statutOffre, String statutEspace, Long proprietaireId) {
        public boolean publique() {
            return "PUBLIE".equals(statutOffre) && "ACTIF".equals(statutEspace);
        }
    }
}
