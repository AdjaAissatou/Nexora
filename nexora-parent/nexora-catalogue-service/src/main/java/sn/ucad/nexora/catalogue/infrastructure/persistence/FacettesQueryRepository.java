package sn.ucad.nexora.catalogue.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.catalogue.application.dto.response.FacettesResponse;
import sn.ucad.nexora.catalogue.application.dto.response.FacettesResponse.Caracteristique;
import sn.ucad.nexora.catalogue.application.dto.response.FacettesResponse.Categorie;
import sn.ucad.nexora.catalogue.application.dto.response.FacettesResponse.Valeur;

/** Comptages des facettes sur un ensemble d'offres (celles d'une recherche). */
@Repository
public class FacettesQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public FacettesResponse facettes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return FacettesResponse.vide();

        List<Categorie> categories = ((List<Object[]>) em.createNativeQuery("""
                SELECT c.id_categorie, c.nom, COUNT(*)
                FROM offre o JOIN categorie c ON c.id_categorie = o.id_categorie
                WHERE o.id_offre IN (:ids)
                GROUP BY c.id_categorie, c.nom
                ORDER BY COUNT(*) DESC, c.nom""").setParameter("ids", ids).getResultList())
                .stream().map(l -> new Categorie(((Number) l[0]).longValue(), (String) l[1], ((Number) l[2]).longValue())).toList();

        // Seules les caractéristiques à choix (listes) et filtrables ; une valeur épuisée ne compte pas
        List<Object[]> lignes = em.createNativeQuery("""
                SELECT a.id_attribut, a.nom, CAST(a.type_champ AS TEXT), v.id_valeur, v.valeur, v.code_couleur,
                       COUNT(DISTINCT oa.id_offre)
                FROM offre_attribut oa
                JOIN attribut a ON a.id_attribut = oa.id_attribut
                JOIN valeur_attribut_possible v ON v.id_valeur = oa.id_valeur
                WHERE oa.id_offre IN (:ids) AND oa.epuise = FALSE
                  AND a.filtrable IS NOT FALSE AND a.actif IS NOT FALSE
                  AND CAST(a.type_champ AS TEXT) IN ('LISTE', 'MULTI_LISTE')
                GROUP BY a.id_attribut, a.nom, a.type_champ, a.ordre_affichage, v.id_valeur, v.valeur, v.code_couleur, v.ordre_affichage
                ORDER BY a.ordre_affichage, a.nom, v.ordre_affichage, v.valeur""").setParameter("ids", ids).getResultList();
        Map<Long, Caracteristique> parAttribut = new LinkedHashMap<>();
        for (Object[] l : lignes) {
            Caracteristique c = parAttribut.computeIfAbsent(((Number) l[0]).longValue(),
                    k -> new Caracteristique(k, (String) l[1], (String) l[2], new ArrayList<>()));
            c.valeurs().add(new Valeur(((Number) l[3]).longValue(), (String) l[4], (String) l[5], ((Number) l[6]).longValue()));
        }
        // Une caractéristique à une seule valeur n'aide pas à choisir
        List<Caracteristique> caracteristiques = parAttribut.values().stream().filter(c -> c.valeurs().size() > 1).toList();

        Object[] prix = (Object[]) em.createNativeQuery("SELECT MIN(prix), MAX(prix) FROM offre WHERE id_offre IN (:ids)")
                .setParameter("ids", ids).getSingleResult();
        return new FacettesResponse(categories, caracteristiques, (BigDecimal) prix[0], (BigDecimal) prix[1]);
    }
}
