package com.nexora.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * DAO d'agregation pour les statistiques et tableaux de bord (comptages
 * globaux plateforme et par espace). Regroupe des requetes JPQL de comptage.
 */
@Stateless
public class StatDao {

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    private long count(String jpql) {
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public long countUtilisateurs() {
        return count("select count(u) from Utilisateur u");
    }

    public long countEspaces() {
        return count("select count(e) from EspaceProfessionnel e");
    }

    public long countEspacesEnAttente() {
        return count("select count(e) from EspaceProfessionnel e where e.verifie = false and e.actif = true");
    }

    public long countProduits() {
        return count("select count(p) from Produit p");
    }

    public long countServices() {
        return count("select count(s) from ServicePro s");
    }

    public long countPromotions() {
        return count("select count(p) from Promotion p where p.actif = true");
    }

    public long countLitigesOuverts() {
        return count("select count(l) from Litige l where lower(l.statut) <> 'resolu' or l.statut is null");
    }
}
