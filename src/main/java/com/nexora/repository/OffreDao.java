package com.nexora.repository;

import com.nexora.common.enums.StatutOffre;
import com.nexora.domain.attribute.OffreAttribut;
import com.nexora.domain.catalog.Offre;
import com.nexora.domain.space.Adresse;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.dto.PageResult;
import com.nexora.dto.RechercheCriteria;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DAO de l'entite {@link Offre}, incluant le moteur de recherche multi-criteres
 * construit dynamiquement avec l'API JPA Criteria. La requete est assemblee a
 * partir d'un {@link RechercheCriteria} : seuls les filtres renseignes sont
 * ajoutes, ce qui garantit generecite et performance.
 *
 * <p>Les attributs dynamiques (EAV) sont filtres via des sous-requetes
 * correlees sur {@link OffreAttribut}, sans aucune colonne figee. La distance
 * geographique est pre-filtree par une "bounding box" (lat/lon) puis raffinee
 * par le service (haversine).</p>
 */
@Stateless
public class OffreDao extends GenericDao<Offre, Long> {

    /** Approximation : 1 degre de latitude ~ 111 km. */
    private static final double KM_PAR_DEGRE = 111.0;

    public PageResult<Offre> rechercher(RechercheCriteria c) {
        CriteriaBuilder cb = em().getCriteriaBuilder();

        // --- Requete principale ---
        CriteriaQuery<Offre> cq = cb.createQuery(Offre.class);
        Root<Offre> offre = cq.from(Offre.class);
        List<Predicate> predicates = construirePredicats(cb, cq, offre, c);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(construireTri(cb, offre, c));

        List<Offre> contenu = em().createQuery(cq.select(offre).distinct(true))
                .setFirstResult(c.getPage() * c.getTaillePage())
                .setMaxResults(c.getTaillePage())
                .getResultList();

        // --- Requete de comptage ---
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<Offre> offreCount = cqCount.from(Offre.class);
        List<Predicate> predicatesCount = construirePredicats(cb, cqCount, offreCount, c);
        cqCount.select(cb.countDistinct(offreCount)).where(predicatesCount.toArray(new Predicate[0]));
        long total = em().createQuery(cqCount).getSingleResult();

        return new PageResult<>(contenu, total, c.getPage(), c.getTaillePage());
    }

    private List<Predicate> construirePredicats(CriteriaBuilder cb, CriteriaQuery<?> cq,
                                                Root<Offre> offre, RechercheCriteria c) {
        List<Predicate> p = new ArrayList<>();

        // On ne montre que les offres publiees et actives.
        p.add(cb.equal(offre.get("statut"), StatutOffre.PUBLIEE));
        p.add(cb.isTrue(offre.get("actif")));

        if (c.getMotCle() != null && !c.getMotCle().isBlank()) {
            String like = "%" + c.getMotCle().toLowerCase() + "%";
            p.add(cb.or(
                    cb.like(cb.lower(offre.get("titre")), like),
                    cb.like(cb.lower(offre.get("description")), like)));
        }
        if (c.getIdCategorie() != null) {
            p.add(cb.equal(offre.get("categorie").get("idCategorie"), c.getIdCategorie()));
        }
        if (c.getIdTypeOffre() != null) {
            p.add(cb.equal(offre.get("typeOffre").get("idTypeOffre"), c.getIdTypeOffre()));
        }
        if (c.getPrixMin() != null) {
            p.add(cb.greaterThanOrEqualTo(offre.get("prix"), c.getPrixMin()));
        }
        if (c.getPrixMax() != null) {
            p.add(cb.lessThanOrEqualTo(offre.get("prix"), c.getPrixMax()));
        }
        if (Boolean.TRUE.equals(c.getDisponibleUniquement())) {
            p.add(cb.isTrue(offre.get("disponible")));
        }

        // --- Filtres portes par l'espace professionnel ---
        if (besoinEspace(c)) {
            Join<Offre, EspaceProfessionnel> espace = offre.join("espace", JoinType.INNER);
            if (Boolean.TRUE.equals(c.getProfessionnelVerifie())) {
                p.add(cb.isTrue(espace.get("verifie")));
            }
            if (Boolean.TRUE.equals(c.getProfessionnelCertifie())) {
                p.add(cb.isTrue(espace.get("certifie")));
            }
            if (c.getNoteMin() != null) {
                p.add(cb.greaterThanOrEqualTo(espace.get("noteMoyenne"), c.getNoteMin().doubleValue()));
            }
            if (besoinAdresse(c)) {
                Join<EspaceProfessionnel, Adresse> adr = espace.join("adresses", JoinType.INNER);
                ajouterFiltresLocalisation(cb, p, adr, c);
            }
        }

        // --- Filtres sur attributs dynamiques (EAV) ---
        if (c.getAttributs() != null) {
            for (Map.Entry<Long, String> e : c.getAttributs().entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank()) continue;
                Subquery<Long> sub = cq.subquery(Long.class);
                Root<OffreAttribut> oa = sub.from(OffreAttribut.class);
                sub.select(oa.get("offre").get("idOffre"));
                sub.where(
                        cb.equal(oa.get("offre"), offre),
                        cb.equal(oa.get("attribut").get("idAttribut"), e.getKey()),
                        cb.or(
                                cb.equal(cb.lower(oa.get("valeurTexte")), e.getValue().toLowerCase()),
                                cb.equal(oa.get("valeurPossible").get("valeur"), e.getValue())));
                p.add(cb.exists(sub));
            }
        }
        return p;
    }

    private void ajouterFiltresLocalisation(CriteriaBuilder cb, List<Predicate> p,
                                            Join<EspaceProfessionnel, Adresse> adr, RechercheCriteria c) {
        if (c.getPays() != null)     p.add(cb.equal(cb.lower(adr.get("pays")), c.getPays().toLowerCase()));
        if (c.getRegion() != null)   p.add(cb.equal(cb.lower(adr.get("region")), c.getRegion().toLowerCase()));
        if (c.getVille() != null)    p.add(cb.equal(cb.lower(adr.get("ville")), c.getVille().toLowerCase()));
        if (c.getQuartier() != null) p.add(cb.equal(cb.lower(adr.get("quartier")), c.getQuartier().toLowerCase()));

        // Pre-filtre par bounding box quand un rayon est demande.
        if (c.getLatitude() != null && c.getLongitude() != null && c.getDistanceMaxKm() != null) {
            double dLat = c.getDistanceMaxKm() / KM_PAR_DEGRE;
            double cosLat = Math.max(0.01, Math.cos(Math.toRadians(c.getLatitude())));
            double dLon = c.getDistanceMaxKm() / (KM_PAR_DEGRE * cosLat);
            p.add(cb.between(adr.get("latitude"),
                    c.getLatitude() - dLat, c.getLatitude() + dLat));
            p.add(cb.between(adr.get("longitude"),
                    c.getLongitude() - dLon, c.getLongitude() + dLon));
        }
    }

    private List<Order> construireTri(CriteriaBuilder cb, Root<Offre> offre, RechercheCriteria c) {
        String tri = c.getTri() == null ? "PERTINENCE" : c.getTri();
        return switch (tri) {
            case "PRIX_ASC"     -> List.of(cb.asc(offre.get("prix")));
            case "PRIX_DESC"    -> List.of(cb.desc(offre.get("prix")));
            case "POPULARITE"   -> List.of(cb.desc(offre.get("nbConsultation")));
            case "RECENT"       -> List.of(cb.desc(offre.get("dateCreation")));
            case "ALPHABETIQUE" -> List.of(cb.asc(offre.get("titre")));
            default             -> List.of(cb.desc(offre.get("scorePertinence")),
                                           cb.desc(offre.get("nbCommandes")));
        };
    }

    private boolean besoinEspace(RechercheCriteria c) {
        return Boolean.TRUE.equals(c.getProfessionnelVerifie())
                || Boolean.TRUE.equals(c.getProfessionnelCertifie())
                || c.getNoteMin() != null
                || besoinAdresse(c);
    }

    private boolean besoinAdresse(RechercheCriteria c) {
        return c.getVille() != null || c.getQuartier() != null || c.getRegion() != null
                || c.getPays() != null
                || (c.getLatitude() != null && c.getLongitude() != null && c.getDistanceMaxKm() != null);
    }
}
