package sn.ucad.nexora.web.dto.catalogue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.StatistiquesDtos} (§26). */
public final class StatistiquesDtos {

    private StatistiquesDtos() {}

    public record Compteurs(long vuesFiches, long vuesDecouvrir, long jaime, long favoris, long appels, long whatsapp,
                            long itineraires, long partages) implements Serializable {
        public long vues() { return vuesFiches + vuesDecouvrir; }
        public long contacts() { return appels + whatsapp + itineraires; }
    }

    public record Semaine(LocalDate debut, Compteurs compteurs) implements Serializable {}

    public record StatOffre(Long id, String titre, String image, boolean populaire, Compteurs compteurs) implements Serializable {}

    public record StatistiquesEspace(Long espaceId, String espaceNom, int semaines, LocalDate debut, List<Semaine> parSemaine,
                                     Compteurs periode, Compteurs precedente, List<StatOffre> offres) implements Serializable {}
}
