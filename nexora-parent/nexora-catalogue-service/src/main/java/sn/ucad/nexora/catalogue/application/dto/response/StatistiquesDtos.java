package sn.ucad.nexora.catalogue.application.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Statistiques détaillées d'un espace (docs/architecture-acteurs.md §26). */
public final class StatistiquesDtos {

    private StatistiquesDtos() {}

    /** Compteurs d'une période (une semaine, ou toute la période choisie). */
    public record Compteurs(long vuesFiches, long vuesDecouvrir, long jaime, long favoris, long appels, long whatsapp,
                            long itineraires, long partages) {
        public long vues() { return vuesFiches + vuesDecouvrir; }
        public long contacts() { return appels + whatsapp + itineraires; }
    }

    /** Une semaine (du lundi {@code debut}). */
    public record Semaine(LocalDate debut, Compteurs compteurs) {}

    public record StatOffre(Long id, String titre, String image, boolean populaire, Compteurs compteurs) {}

    /**
     * {@code periode} : les {@code semaines} dernières semaines (semaine en cours comprise) ;
     * {@code precedente} : les {@code semaines} d'avant, pour l'évolution.
     */
    public record StatistiquesEspace(Long espaceId, String espaceNom, int semaines, LocalDate debut,
                                     List<Semaine> parSemaine, Compteurs periode, Compteurs precedente,
                                     List<StatOffre> offres) {}

    /** Clic relayé par le web : APPEL, WHATSAPP, ITINERAIRE ou PARTAGE, sur une offre ou un espace. */
    public record Evenement(String type, Long idOffre, Long idEspace, String visiteur) {}
}
