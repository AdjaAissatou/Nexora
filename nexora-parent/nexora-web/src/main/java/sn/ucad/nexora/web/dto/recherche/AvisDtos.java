package sn.ucad.nexora.web.dto.recherche;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.recherche.application.dto.response.AvisDtos} (§22). */
public final class AvisDtos {

    private AvisDtos() {}

    /** Un de mes avis, même masqué ; {@code cibleVisible} : faux si l'espace ou l'offre n'est plus consultable. */
    public record MonAvisDetail(Long id, int note, String commentaire, LocalDateTime dateCreation,
                                LocalDateTime dateModification, boolean masque, String motifModeration,
                                String reponse, LocalDateTime dateReponse, Long espaceId, String espaceNom,
                                Long offreId, String offreTitre, String image, boolean cibleVisible) implements Serializable {
        public boolean avecReponse() { return reponse != null && !reponse.isBlank(); }

        /** Fiche de l'offre notée, sinon de l'espace. */
        public String lien() { return offreId != null ? "/offre.xhtml?id=" + offreId : "/espace.xhtml?id=" + espaceId; }
    }

    public record AvisRecu(Long id, int note, String commentaire, String auteur, LocalDateTime dateCreation,
                           LocalDateTime dateModification, Long offreId, String offreTitre, String reponse,
                           LocalDateTime dateReponse) implements Serializable {
        public boolean avecReponse() { return reponse != null && !reponse.isBlank(); }
    }

    /** {@code repartition} : nombre d'avis à 1, 2, 3, 4 et 5 étoiles, dans cet ordre. */
    public record AvisRecus(Long espaceId, String espaceNom, double moyenne, int nombre, List<Integer> repartition,
                            int sansReponse, int masques, List<AvisRecu> avis) implements Serializable {}
}
