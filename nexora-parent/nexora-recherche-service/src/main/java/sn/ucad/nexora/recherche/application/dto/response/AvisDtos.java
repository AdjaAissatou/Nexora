package sn.ucad.nexora.recherche.application.dto.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/** « Mes avis » côté client et « Avis reçus » côté professionnel (docs/architecture-acteurs.md §22). */
public final class AvisDtos {

    private AvisDtos() {}

    /**
     * Un avis vu par son auteur, même masqué (avec le motif) : sur quoi il porte, la réponse du
     * professionnel. {@code image} : couverture ou logo de l'espace, sinon photo de l'offre.
     * {@code cibleVisible} : faux si l'espace ou l'offre n'est plus consultable.
     */
    public record MonAvisDetail(Long id, int note, String commentaire, LocalDateTime dateCreation,
                                LocalDateTime dateModification, boolean masque, String motifModeration,
                                String reponse, LocalDateTime dateReponse, Long espaceId, String espaceNom,
                                Long offreId, String offreTitre, String image, boolean cibleVisible) {}

    /** Un avis reçu par un espace (sur lui ou sur l'une de ses offres), tel que le professionnel le voit. */
    public record AvisRecu(Long id, int note, String commentaire, String auteur, LocalDateTime dateCreation,
                           LocalDateTime dateModification, Long offreId, String offreTitre, String reponse,
                           LocalDateTime dateReponse) {}

    /**
     * Les avis visibles d'un espace et leur synthèse. {@code repartition} : nombre d'avis à 1, 2, 3, 4
     * et 5 étoiles (dans cet ordre) ; {@code masques} : avis retirés par la modération (non listés).
     */
    public record AvisRecus(Long espaceId, String espaceNom, double moyenne, int nombre, List<Integer> repartition,
                            int sansReponse, int masques, List<AvisRecu> avis) {}

    /** Modifier son avis : nouvelle note et nouveau commentaire. */
    public record ModifierAvisRequest(@NotNull @Min(1) @Max(5) Integer note, String commentaire) {}

    /** Réponse publique du professionnel à un avis. */
    public record ReponseRequest(String texte) {}
}
