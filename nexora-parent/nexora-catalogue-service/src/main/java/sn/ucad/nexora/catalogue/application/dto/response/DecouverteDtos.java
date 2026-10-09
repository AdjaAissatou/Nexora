package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** Nexora Découvrir (§18) : le flux vertical, une carte à la fois. */
public final class DecouverteDtos {

    private DecouverteDtos() {}

    /**
     * Une carte du flux. {@code type} : PRODUIT, SERVICE, PRESTATION (service « clé en main » :
     * mariage, décoration, pack…) ou ESPACE (un professionnel). {@code section} : PERTINENT (goûts),
     * TENDANCE (nouveautés et succès du coin) ou DECOUVERTE (hors des habitudes).
     */
    public record Carte(String type, String section, Long idOffre, Long idEspace, String titre, String description,
                        BigDecimal prix, BigDecimal ancienPrix, List<String> medias, String categorie, String rayon,
                        String espaceNom, String espaceLogo, boolean espaceVerifie, boolean espaceCertifie,
                        BigDecimal note, Integer nombreAvis, String typeEspace, String commune, String quartier,
                        Double distanceKm, Boolean ouvertMaintenant, String telephone, String accroche, String raison,
                        List<Caracteristique> caracteristiques, boolean reservation, boolean domicile, long nombreOffres,
                        /** Popularité (§23) : j'aime reçus (affichés sous le cœur) et badge « 🔥 Populaire ». */
                        long nombreJaime, boolean populaire) {}

    public record Caracteristique(String nom, List<String> valeurs) {}

    /** {@code fin} : plus rien de nouveau dans cette zone pour ce visiteur. */
    public record Flux(List<Carte> cartes, boolean fin) {}

    /** Ce que Nexora a compris des goûts du visiteur (affiché tel quel : pas de boîte noire). */
    public record Profil(List<Interet> interets, long nombreSignaux) {}

    public record Interet(String nom, double poids) {}

    public record Signal(String visiteur, Long idOffre, Long idEspace, String type, Integer dureeMs) {}
}
