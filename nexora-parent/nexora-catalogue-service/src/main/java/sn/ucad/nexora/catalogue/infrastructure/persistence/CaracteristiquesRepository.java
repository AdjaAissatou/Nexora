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
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse.Caracteristique;
import sn.ucad.nexora.catalogue.application.dto.response.OffreDetailResponse.Valeur;

/**
 * Caractéristiques renseignées d'une offre (tailles, couleurs, matière…), regroupées par
 * caractéristique, de la catégorie la plus générale à la plus précise. Chaque valeur porte
 * sa pastille de couleur éventuelle et indique si elle est momentanément épuisée.
 */
@Repository
public class CaracteristiquesRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Caracteristique> deOffre(Long idOffre) {
        List<Object[]> lignes = em.createNativeQuery("""
                WITH RECURSIVE ancetres(id, profondeur) AS (
                    SELECT o.id_categorie, 0 FROM offre o WHERE o.id_offre = :id
                    UNION ALL
                    SELECT c.id_categorie_parent, a.profondeur + 1 FROM categorie c JOIN ancetres a ON c.id_categorie = a.id
                    WHERE c.id_categorie_parent IS NOT NULL
                )
                SELECT a.id_attribut, a.nom, CAST(a.type_champ AS TEXT), a.unite,
                       v.valeur, oa.valeur_texte, oa.valeur_nombre, v.code_couleur, oa.epuise
                FROM offre_attribut oa
                JOIN attribut a ON a.id_attribut = oa.id_attribut
                LEFT JOIN valeur_attribut_possible v ON v.id_valeur = oa.id_valeur
                LEFT JOIN ancetres an ON an.id = a.id_categorie
                WHERE oa.id_offre = :id AND a.affichable IS NOT FALSE AND a.actif IS NOT FALSE
                ORDER BY COALESCE(an.profondeur, -1) DESC, a.ordre_affichage, a.nom, v.ordre_affichage, oa.id_offre_attribut""")
                .setParameter("id", idOffre).getResultList();
        Map<Long, Caracteristique> parAttribut = new LinkedHashMap<>();
        for (Object[] l : lignes) {
            long id = ((Number) l[0]).longValue();
            String type = (String) l[2];
            String valeur = valeur(type, (String) l[4], (String) l[5], (BigDecimal) l[6]);
            if (valeur == null) continue;
            Caracteristique c = parAttribut.computeIfAbsent(id,
                    k -> new Caracteristique((String) l[1], type, (String) l[3], new ArrayList<>()));
            if (c.getValeurs().stream().noneMatch(v -> v.getLibelle().equals(valeur)))
                c.getValeurs().add(new Valeur(valeur, (String) l[7], Boolean.TRUE.equals(l[8])));
        }
        return new ArrayList<>(parAttribut.values());
    }

    private static String valeur(String type, String valeurListe, String texte, BigDecimal nombre) {
        if (valeurListe != null) return valeurListe;
        if ("BOOLEAN".equals(type) && texte != null) return Boolean.parseBoolean(texte.trim()) ? "Oui" : "Non";
        if (nombre != null) return nombre.stripTrailingZeros().toPlainString();
        return texte == null || texte.isBlank() ? null : texte.trim();
    }
}
