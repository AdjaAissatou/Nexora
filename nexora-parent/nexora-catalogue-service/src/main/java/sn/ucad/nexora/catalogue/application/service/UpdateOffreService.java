package sn.ucad.nexora.catalogue.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.ucad.nexora.catalogue.application.dto.request.AttributValeurRequest;
import sn.ucad.nexora.catalogue.application.dto.request.UpdateOffreRequest;
import sn.ucad.nexora.catalogue.application.dto.response.CreateOffreResponse;
import sn.ucad.nexora.catalogue.application.usecase.UpdateOffreUseCase;
import sn.ucad.nexora.catalogue.infrastructure.persistence.EspaceLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.UtilisateurLookupRepository;
import sn.ucad.nexora.catalogue.infrastructure.persistence.entity.EspaceLookupEntity;

import java.util.List;
import java.util.UUID;

/** Modification d'une offre existante par le propriétaire de son espace. Même logique d'écriture
 * native que CreateOffreService ; remplace entièrement produit/service et les attributs plutôt
 * que de les fusionner, pour rester cohérent si la catégorie ou le type d'offre a changé. */
@Service
public class UpdateOffreService implements UpdateOffreUseCase {

    private final EntityManager em;
    private final UtilisateurLookupRepository utilisateurRepository;
    private final EspaceLookupRepository espaceRepository;

    public UpdateOffreService(EntityManager em, UtilisateurLookupRepository utilisateurRepository,
                               EspaceLookupRepository espaceRepository) {
        this.em = em;
        this.utilisateurRepository = utilisateurRepository;
        this.espaceRepository = espaceRepository;
    }

    @Override
    @Transactional
    public CreateOffreResponse modifier(UUID accountId, Long idOffre, UpdateOffreRequest r) {
        if (accountId == null) {
            throw new IllegalArgumentException("Compte authentifié obligatoire");
        }
        if (r.getTitre() == null || r.getTitre().isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (r.getIdTypeOffre() == null || r.getIdCategorie() == null) {
            throw new IllegalArgumentException("Type d'offre et catégorie sont obligatoires");
        }

        Long utilisateurId = utilisateurRepository.findIdByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profil utilisateur introuvable"));

        Long idEspace = idEspaceDeLoffre(idOffre);
        EspaceLookupEntity espace = espaceRepository.findById(idEspace)
                .orElseThrow(() -> new IllegalArgumentException("Espace introuvable"));
        if (!utilisateurId.equals(espace.getUtilisateurId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette offre");
        }

        String principale = verifierEtObtenirPrincipale(r.getIdTypeOffre(), r.getIdCategorie());

        mettreAJourOffre(idOffre, r);

        Query supprimerProduit = em.createNativeQuery("DELETE FROM produit WHERE id_offre = :id");
        supprimerProduit.setParameter("id", idOffre).executeUpdate();
        Query supprimerService = em.createNativeQuery("DELETE FROM service WHERE id_offre = :id");
        supprimerService.setParameter("id", idOffre).executeUpdate();

        if ("SERVICE".equals(principale)) {
            insererService(idOffre, r);
        } else {
            insererProduit(idOffre, r);
        }

        Query supprimerAttributs = em.createNativeQuery("DELETE FROM offre_attribut WHERE id_offre = :id");
        supprimerAttributs.setParameter("id", idOffre).executeUpdate();
        insererAttributs(idOffre, r.getAttributs());

        Query supprimerImages = em.createNativeQuery("DELETE FROM image WHERE id_offre = :id");
        supprimerImages.setParameter("id", idOffre).executeUpdate();
        insererImages(idOffre, r.getImages());

        return new CreateOffreResponse(idOffre, r.getTitre());
    }

    private Long idEspaceDeLoffre(Long idOffre) {
        Query q = em.createNativeQuery("SELECT id_espace FROM offre WHERE id_offre = :id");
        q.setParameter("id", idOffre);
        try {
            return ((Number) q.getSingleResult()).longValue();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("Offre introuvable");
        }
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

    private void mettreAJourOffre(Long idOffre, UpdateOffreRequest r) {
        Query q = em.createNativeQuery("""
                UPDATE offre SET id_type_offre = :idTypeOffre, id_categorie = :idCategorie, titre = :titre,
                       description = :description, prix = :prix, negociable = :negociable,
                       disponible = :disponible, date_modification = NOW()
                WHERE id_offre = :idOffre
                """);
        q.setParameter("idTypeOffre", r.getIdTypeOffre());
        q.setParameter("idCategorie", r.getIdCategorie());
        q.setParameter("titre", r.getTitre().trim());
        q.setParameter("description", r.getDescription());
        q.setParameter("prix", r.getPrix());
        q.setParameter("negociable", r.isNegociable());
        q.setParameter("disponible", r.isDisponible());
        q.setParameter("idOffre", idOffre);
        q.executeUpdate();
    }

    private void insererProduit(Long idOffre, UpdateOffreRequest r) {
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

    private void insererService(Long idOffre, UpdateOffreRequest r) {
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

    private void insererImages(Long idOffre, List<String> images) {
        if (images == null) return;
        int ordre = 0;
        for (String url : images) {
            if (url == null || url.isBlank()) continue;
            Query q = em.createNativeQuery("""
                    INSERT INTO image (id_offre, url, principale, ordre_affichage)
                    VALUES (:idOffre, :url, :principale, :ordre)
                    """);
            q.setParameter("idOffre", idOffre);
            q.setParameter("url", url.trim());
            q.setParameter("principale", ordre == 0);
            q.setParameter("ordre", ordre);
            q.executeUpdate();
            ordre++;
        }
    }
}
