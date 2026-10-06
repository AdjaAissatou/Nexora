package sn.ucad.nexora.catalogue.domain.repository;

import sn.ucad.nexora.catalogue.domain.entity.Offre;

import java.util.List;
import java.util.Optional;

/**
 * Port secondaire (driven) — interface du dépôt d'offres.
 * L'implémentation se trouve dans infrastructure/persistence.
 */
public interface OffreRepository {

    Optional<Offre> findById(Long id);

    /**
     * Recherche plein texte multicritère avec filtres.
     * Utilise la vue vue_recherche_globale + ILIKE sur titre/description/categorie/commune.
     */
    List<Offre> search(RechercheParams params);

    /** Nombre total de résultats de la recherche, toutes pages confondues. */
    long count(RechercheParams params);

    /**
     * Incrémenter le compteur de vues d'une offre.
     */
    void incrementerVues(Long idOffre);
}
