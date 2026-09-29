package sn.ucad.nexora.catalogue.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.response.AttributValeurResponse;
import sn.ucad.nexora.catalogue.application.dto.response.OffreEditionResponse;
import sn.ucad.nexora.catalogue.application.usecase.GetOffreEditionUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.EspaceLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.EspaceLookupEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Chargement d'une offre pour édition — réservé au propriétaire de l'espace, contrairement à la
 * fiche publique (GetOffreService) : reconstitue la chaîne de catégories (racine > sous-catégorie
 * > feuille) pour préremplir la cascade du formulaire, et les valeurs d'attributs déjà saisies.
 */
@Service
public class GetOffreEditionService implements GetOffreEditionUseCase {

    private final EntityManager em;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final EspaceLookupRepository espaceRepository;

    public GetOffreEditionService(EntityManager em, UtilisateurLookupRepository utilisateurRepository,
                                   EspaceLookupRepository espaceRepository) {
        this.em = em;
        this.utilisateurRepository = utilisateurRepository;
        this.espaceRepository = espaceRepository;
    }

    @Override
    public OffreEditionResponse obtenir(UUID accountId, Long idOffre) {
        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }
        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        Query q = em.createNativeQuery("""
                SELECT id_offre, id_espace, id_categorie, id_type_offre, titre, description, prix, negociable, disponible
                FROM offre WHERE id_offre = :id
                """);
        q.setParameter("id", idOffre);
        Object[] row;
        try {
            row = (Object[]) q.getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Offre introuvable");
        }

        Long idEspace = toLong(row[1]);
        EspaceLookupEntity espace = espaceRepository.findById(idEspace)
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));
        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette offre");
        }

        OffreEditionResponse r = new OffreEditionResponse();
        r.setId(toLong(row[0]));
        r.setIdEspace(idEspace);
        r.setIdCategorie(toLong(row[2]));
        r.setIdTypeOffre(toLong(row[3]));
        r.setTitre((String) row[4]);
        r.setDescription((String) row[5]);
        r.setPrix((BigDecimal) row[6]);
        r.setNegociable((Boolean) row[7]);
        r.setDisponible((Boolean) row[8]);

        remplirChaineCategories(r);
        remplirProduitOuService(r);
        r.setAttributs(chargerAttributs(idOffre));
        r.setImages(chargerImages(idOffre));

        return r;
    }

    /** Racine (niveau1), sous-catégorie (niveau2) et feuille (niveau3) — la feuille peut être niveau1 ou niveau2 si la catégorie est moins profonde. */
    private void remplirChaineCategories(OffreEditionResponse r) {
        Query q = em.createNativeQuery("""
                WITH RECURSIVE chaine AS (
                    SELECT id_categorie, id_categorie_parent, nom, 0 AS profondeur
                    FROM categorie WHERE id_categorie = :id
                    UNION ALL
                    SELECT c.id_categorie, c.id_categorie_parent, c.nom, chaine.profondeur + 1
                    FROM categorie c JOIN chaine ON c.id_categorie = chaine.id_categorie_parent
                )
                SELECT id_categorie FROM chaine ORDER BY profondeur DESC
                """);
        q.setParameter("id", r.getIdCategorie());
        @SuppressWarnings("unchecked")
        List<Object> ids = q.getResultList();
        List<Long> chaine = ids.stream().map(this::toLong).toList();
        if (chaine.size() >= 1) r.setNiveau1Id(chaine.get(0));
        if (chaine.size() >= 2) r.setNiveau2Id(chaine.get(1));
        if (chaine.size() >= 3) r.setNiveau3Id(chaine.get(2));
    }

    private void remplirProduitOuService(OffreEditionResponse r) {
        Query qp = em.createNativeQuery("""
                SELECT marque, modele, reference, quantite_stock, garantie, neuf FROM produit WHERE id_offre = :id
                """);
        qp.setParameter("id", r.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> produits = qp.getResultList();
        if (!produits.isEmpty()) {
            Object[] p = produits.get(0);
            r.setMarque((String) p[0]);
            r.setModele((String) p[1]);
            r.setReference((String) p[2]);
            r.setQuantiteStock(p[3] == null ? null : ((Number) p[3]).intValue());
            r.setGarantie((String) p[4]);
            r.setNeuf((Boolean) p[5]);
            return;
        }

        Query qs = em.createNativeQuery("""
                SELECT duree_estimee, intervention_domicile, reservation FROM service WHERE id_offre = :id
                """);
        qs.setParameter("id", r.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> services = qs.getResultList();
        if (!services.isEmpty()) {
            Object[] s = services.get(0);
            r.setDureeEstimee(s[0] == null ? null : ((Number) s[0]).intValue());
            r.setInterventionDomicile((Boolean) s[1]);
            r.setReservation((Boolean) s[2]);
        }
    }

    @SuppressWarnings("unchecked")
    private List<AttributValeurResponse> chargerAttributs(Long idOffre) {
        Query q = em.createNativeQuery("""
                SELECT id_attribut, valeur_texte, valeur_nombre, CAST(valeur_date AS TEXT), id_valeur
                FROM offre_attribut WHERE id_offre = :id
                """);
        q.setParameter("id", idOffre);
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(row -> {
            AttributValeurResponse a = new AttributValeurResponse();
            a.setIdAttribut(toLong(row[0]));
            a.setValeurTexte((String) row[1]);
            a.setValeurNombre((BigDecimal) row[2]);
            a.setValeurDate((String) row[3]);
            a.setIdValeur(toLong(row[4]));
            return a;
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<String> chargerImages(Long idOffre) {
        Query q = em.createNativeQuery(
                "SELECT url FROM image WHERE id_offre = :id ORDER BY ordre_affichage");
        q.setParameter("id", idOffre);
        return q.getResultList();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
