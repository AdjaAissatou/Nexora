package com.nexora.service.impl;

import com.nexora.domain.messaging.Notification;
import com.nexora.domain.user.Utilisateur;
import com.nexora.dto.NotificationDTO;
import com.nexora.service.NotificationService;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

/** Implémentation transactionnelle des notifications utilisateur. */
@Stateless
public class NotificationServiceImpl implements NotificationService {

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    @Override
    public void notifier(Long idUtilisateur, String contenu, String lien, String importance) {
        if (idUtilisateur == null) return;
        Notification n = new Notification();
        n.setUtilisateur(em.getReference(Utilisateur.class, idUtilisateur));
        n.setContenu(contenu);
        n.setLien(lien);
        n.setImportance(importance != null ? importance : "INFO");
        n.setDateCreation(LocalDateTime.now());
        n.setLu(false);
        em.persist(n);
    }

    @Override
    public List<NotificationDTO> pourUtilisateur(Long idUtilisateur) {
        return em.createQuery(
                        "select n from Notification n where n.utilisateur.idUtilisateur = :id "
                                + "order by n.dateCreation desc", Notification.class)
                .setParameter("id", idUtilisateur)
                .setMaxResults(50)
                .getResultList().stream().map(NotificationDTO::new).toList();
    }

    @Override
    public long nbNonLues(Long idUtilisateur) {
        if (idUtilisateur == null) return 0;
        return em.createQuery(
                        "select count(n) from Notification n "
                                + "where n.utilisateur.idUtilisateur = :id and n.lu = false", Long.class)
                .setParameter("id", idUtilisateur)
                .getSingleResult();
    }

    @Override
    public void marquerToutesLues(Long idUtilisateur) {
        em.createQuery("update Notification n set n.lu = true, n.dateLecture = :now "
                        + "where n.utilisateur.idUtilisateur = :id and n.lu = false")
                .setParameter("now", LocalDateTime.now())
                .setParameter("id", idUtilisateur)
                .executeUpdate();
    }
}
