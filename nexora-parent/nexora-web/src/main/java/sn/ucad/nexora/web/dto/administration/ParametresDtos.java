package sn.ucad.nexora.web.dto.administration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Miroirs des réponses de {@code ParametresService} (administration-service) et de
 * {@code ReglesJustificatifsService} (espace-service), docs/architecture-acteurs.md §9.12.
 */
public final class ParametresDtos {

    private ParametresDtos() {}

    public record Parametre(String code, String libelle, String description, String categorie, String genre, int min, int max,
                            String valeur, LocalDateTime dateModification) {}

    public record TypeJustificatif(Long id, String code, String libelle, String description, boolean actif, long regles, long documents) {}

    public record TypeEspace(Long id, String nom) {}

    public record Regle(Long id, Long typeEspaceId, String typeEspace, Long typeJustificatifId, String code, String libelle,
                        boolean obligatoire, String groupeAlternatif) {}

    public record VueJustificatifs(List<TypeJustificatif> types, List<TypeEspace> typesEspace, List<Regle> regles) {}
}
