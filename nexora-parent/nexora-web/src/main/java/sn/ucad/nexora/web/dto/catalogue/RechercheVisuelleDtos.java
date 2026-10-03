package sn.ucad.nexora.web.dto.catalogue;

import java.util.List;

/** Miroirs des réponses de RechercheVisuelleService (catalogue-service), §11. */
public final class RechercheVisuelleDtos {

    private RechercheVisuelleDtos() {}

    /** Une offre et sa ressemblance avec la photo (cosinus, de 0 à 1). */
    public record Resultat(OffreSummaryResponse offre, double ressemblance) {}

    public record Reponse(List<Resultat> resultats, long imagesIndexees, long imagesEnAttente) {}
}
