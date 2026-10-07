package sn.ucad.nexora.web.dto.administration;

import java.io.Serializable;
import java.util.List;

/** Miroir de {@code sn.ucad.nexora.catalogue.application.dto.response.admin.CatalogueAdminDtos} (§9.11). */
public final class CatalogueAdminDtos {

    private CatalogueAdminDtos() {}

    public record Noeud(Long id, Long parentId, String nom, String description, String icone, String couleur, int ordre,
                        boolean actif, long sousCategories, long typesOffre, long attributs, long offres) implements Serializable {

        /** Rien n'en dépend : la suppression est permise. */
        public boolean vide() {
            return sousCategories == 0 && typesOffre == 0 && attributs == 0 && offres == 0;
        }
    }

    public record Etape(Long id, String nom, boolean actif) implements Serializable {}

    public record TypeOffreAdmin(Long id, String libelle, String description, String principale, boolean actif, long offres) implements Serializable {}

    public record ValeurAdmin(Long id, String valeur, int ordre, boolean actif, long utilisations) implements Serializable {}

    public record AttributAdmin(Long id, String nom, String code, String typeChamp, boolean obligatoire, boolean filtrable,
                                String unite, String aide, int ordre, boolean actif, long utilisations,
                                List<ValeurAdmin> valeurs) implements Serializable {

        public boolean liste() {
            return "LISTE".equals(typeChamp) || "MULTI_LISTE".equals(typeChamp);
        }
    }

    public record TypeEspaceLien(Long id, String nom, boolean lie) implements Serializable {}

    public record FicheCategorie(Noeud categorie, List<Etape> chemin, boolean parentsActifs, List<Noeud> sousCategories,
                                 List<TypeOffreAdmin> typesOffre, List<AttributAdmin> attributs,
                                 List<TypeEspaceLien> typesEspace) implements Serializable {}

    public record Resultat(String genre, Long id, Long categorieId, String libelle, String chemin, boolean actif) implements Serializable {}

    public record CategorieRequest(Long parentId, String nom, String description, String icone, String couleur, Integer ordre) {}

    public record TypeOffreRequest(String libelle, String description, String principale) {}

    public record AttributRequest(String nom, String typeChamp, Boolean obligatoire, Boolean filtrable, String unite, String aide, Integer ordre) {}

    public record ValeurRequest(String valeur, Integer ordre) {}

    public record MotifRequest(String motif) {}

    // ------------------------------------------------------------------ catégories proposées (« Autre… », §21)

    public record OffreProposee(Long id, String titre, Long espaceId, String espace, String nature, String texte)
            implements Serializable {}

    public record Proposition(String cle, String libelle, List<String> variantes, int nombreOffres, int nombreEspaces,
                              Long categorieActuelleId, String categorieActuelle, String nature,
                              java.time.LocalDateTime premiere, List<OffreProposee> offres) implements Serializable {}

    public record PropositionCreation(String cle, Long parentId, String nom, String principale) {}

    public record PropositionRattachement(String cle, Long categorieId) {}

    public record PropositionEcart(String cle, String motif) {}
}
