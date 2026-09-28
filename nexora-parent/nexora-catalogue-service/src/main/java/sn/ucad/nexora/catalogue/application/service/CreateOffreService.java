package sn.ucad.nexora.catalogue.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.request.AttributValeurRequest;
import sn.ucad.nexora.catalogue.application.dto.request.CreateOffreRequest;
import sn.ucad.nexora.catalogue.application.dto.response.CreateOffreResponse;
import sn.ucad.nexora.catalogue.application.usecase.CreateOffreUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.EspaceLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.EspaceLookupEntity;

import java.util.List;
import java.util.UUID;

/**
 * Création d'une offre (produit ou service) par le propriétaire de l'espace.
 * Écrit directement en SQL natif (offre + produit/service + offre_attribut) : cohérent
 * avec OffreRepositoryAdapter, qui lit déjà ce schéma sans jamais passer par des entités JPA riches.
 */
@Service
public class CreateOffreService implements CreateOffreUseCase {

    private final EntityManager em;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final EspaceLookupRepository espaceRepository;

    public CreateOffreService(EntityManager em, UtilisateurLookupRepository utilisateurRepository,
                               EspaceLookupRepository espaceRepository) {
        this.em = em;
        this.utilisateurRepository = utilisateurRepository;
        this.espaceRepository = espaceRepository;
    }

    @Override
    @Transactional
    public CreateOffreResponse creer(UUID accountId, CreateOffreRequest r) {
        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }
        if (r.getTitre() == null || r.getTitre().isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (r.getIdEspace() == null || r.getIdTypeOffre() == null || r.getIdCategorie() == null) {
            throw new IllegalArgumentException("Espace, type d'offre et catégorie sont obligatoires");
        }

        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        EspaceLookupEntity espace = espaceRepository.findById(r.getIdEspace())
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));

        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à publier une offre pour cet espace");
        }

        String principale = verifierEtObtenirPrincipale(r.getIdTypeOffre(), r.getIdCategorie());

        Long idOffre = inserer(r);

        if ("SERVICE".equals(principale)) {
            insererService(idOffre, r);
        } else {
            insererProduit(idOffre, r);
        }

        insererAttributs(idOffre, r.getAttributs());

        return new CreateOffreResponse(idOffre, r.getTitre());
    }

    private String verifierEtObtenirPrincipale(Long idTypeOffre, Long idCategorie) {
        Query q = em.createNativeQuery(
                "SELECT CAST(principale AS TEXT), id_categorie FROM type_offre WHERE id_type_offre = :id");
        q.setParameter("id", idTypeOffre);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Type d'offre introuvable");
        }
        Object[] row = rows.get(0);
        Long categorieDuType = ((Number) row[1]).longValue();
        if (!categorieDuType.equals(idCategorie)) {
            throw new IllegalArgumentException("Ce type d'offre n'appartient pas à la catégorie choisie");
        }
        return (String) row[0];
    }

    private Long inserer(CreateOffreRequest r) {
        Query q = em.createNativeQuery("""
                INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix,
                                    negociable, disponible, est_commandable, statut, date_creation,
                                    date_modification, date_publication)
                VALUES (:idEspace, :idTypeOffre, :idCategorie, :titre, :description, :prix,
                        :negociable, :disponible, TRUE, 'PUBLIE', NOW(), NOW(), NOW())
                RETURNING id_offre
                """);
        q.setParameter("idEspace", r.getIdEspace());
        q.setParameter("idTypeOffre", r.getIdTypeOffre());
        q.setParameter("idCategorie", r.getIdCategorie());
        q.setParameter("titre", r.getTitre().trim());
        q.setParameter("description", r.getDescription());
        q.setParameter("prix", r.getPrix());
        q.setParameter("negociable", r.isNegociable());
        q.setParameter("disponible", r.isDisponible());
        return ((Number) q.getSingleResult()).longValue();
    }

    private void insererProduit(Long idOffre, CreateOffreRequest r) {
        Query q = em.createNativeQuery("""
                INSERT INTO produit (id_offre, marque, modele, reference, quantite_stock, garantie, neuf)
                VALUES (:idOffre, :marque, :modele, :reference, :quantiteStock, :garantie, :neuf)
                """);
        q.setParameter("idOffre", idOffre);
        q.setParameter("marque", r.getMarque());
        q.setParameter("modele", r.getModele());
        q.setParameter("reference", r.getReference());
        q.setParameter("quantiteStock", r.getQuantiteStock() == null ? 0 : r.getQuantiteStock());
        q.setParameter("garantie", r.getGarantie());
        q.setParameter("neuf", r.getNeuf() == null || r.getNeuf());
        q.executeUpdate();
    }

    private void insererService(Long idOffre, CreateOffreRequest r) {
        Query q = em.createNativeQuery("""
                INSERT INTO service (id_offre, duree_estimee, intervention_domicile, reservation)
                VALUES (:idOffre, :dureeEstimee, :interventionDomicile, :reservation)
                """);
        q.setParameter("idOffre", idOffre);
        q.setParameter("dureeEstimee", r.getDureeEstimee());
        q.setParameter("interventionDomicile", r.getInterventionDomicile() != null && r.getInterventionDomicile());
        q.setParameter("reservation", r.getReservation() == null || r.getReservation());
        q.executeUpdate();
    }

    private void insererAttributs(Long idOffre, List<AttributValeurRequest> attributs) {
        if (attributs == null) return;
        for (AttributValeurRequest a : attributs) {
            if (a.getIdAttribut() == null) continue;
            boolean vide = a.getValeurTexte() == null && a.getValeurNombre() == null
                    && a.getValeurDate() == null && a.getIdValeur() == null;
            if (vide) continue;

            Query q = em.createNativeQuery("""
                    INSERT INTO offre_attribut (id_offre, id_attribut, valeur_texte, valeur_nombre, valeur_date, id_valeur)
                    VALUES (:idOffre, :idAttribut, :valeurTexte, :valeurNombre,
                            CAST(:valeurDate AS DATE), :idValeur)
                    """);
            q.setParameter("idOffre", idOffre);
            q.setParameter("idAttribut", a.getIdAttribut());
            q.setParameter("valeurTexte", a.getValeurTexte());
            q.setParameter("valeurNombre", a.getValeurNombre());
            q.setParameter("valeurDate", a.getValeurDate());
            q.setParameter("idValeur", a.getIdValeur());
            q.executeUpdate();
        }
    }
}
