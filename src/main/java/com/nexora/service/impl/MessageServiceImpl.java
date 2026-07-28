package com.nexora.service.impl;

import com.nexora.domain.messaging.Conversation;
import com.nexora.domain.messaging.Message;
import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.domain.user.Utilisateur;
import com.nexora.dto.ConversationDTO;
import com.nexora.dto.MessageDTO;
import com.nexora.service.MessageService;
import com.nexora.service.NotificationService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

/** Implémentation transactionnelle de la messagerie. */
@Stateless
public class MessageServiceImpl implements MessageService {

    @PersistenceContext(unitName = "nexoraPU")
    private EntityManager em;

    @EJB
    private NotificationService notificationService;

    @Override
    public List<ConversationDTO> conversations(Long idUtilisateur) {
        List<Conversation> convs = em.createQuery(
                        "select c from Conversation c "
                                + "where c.client.idUtilisateur = :id "
                                + "   or c.espace.proprietaire.idUtilisateur = :id "
                                + "order by c.dateDernierMessage desc nulls last, c.dateCreation desc",
                        Conversation.class)
                .setParameter("id", idUtilisateur)
                .getResultList();
        return convs.stream().map(c -> {
            ConversationDTO d = new ConversationDTO();
            d.setId(c.getIdConversation());
            d.setDernierMessage(c.getDernierMessage());
            d.setDateDernierMessage(c.getDateDernierMessage());
            boolean estClient = c.getClient() != null
                    && c.getClient().getIdUtilisateur().equals(idUtilisateur);
            if (estClient) {
                d.setTitre(c.getEspace() != null ? c.getEspace().getNomCommercial() : "Espace");
            } else {
                Utilisateur cl = c.getClient();
                String nom = cl == null ? "Client"
                        : (cl.getPrenom() != null && !cl.getPrenom().isBlank() ? cl.getPrenom() : cl.getNom());
                d.setTitre(nom != null ? nom : "Client");
            }
            return d;
        }).toList();
    }

    @Override
    public Long ouvrirAvecEspace(Long idClient, Long idEspace) {
        List<Conversation> ex = em.createQuery(
                        "select c from Conversation c "
                                + "where c.client.idUtilisateur = :cl and c.espace.idEspace = :es",
                        Conversation.class)
                .setParameter("cl", idClient).setParameter("es", idEspace)
                .setMaxResults(1).getResultList();
        if (!ex.isEmpty()) return ex.get(0).getIdConversation();
        Conversation c = new Conversation();
        c.setClient(em.getReference(Utilisateur.class, idClient));
        c.setEspace(em.getReference(EspaceProfessionnel.class, idEspace));
        c.setDateCreation(LocalDateTime.now());
        em.persist(c);
        return c.getIdConversation();
    }

    @Override
    public List<MessageDTO> messages(Long idConversation, Long idLecteur) {
        List<Message> msgs = em.createQuery(
                        "select m from Message m where m.conversation.idConversation = :id "
                                + "order by m.dateEnvoi asc", Message.class)
                .setParameter("id", idConversation)
                .getResultList();
        return msgs.stream().map(m -> {
            boolean deMoi = m.getExpediteur() != null
                    && m.getExpediteur().getIdUtilisateur().equals(idLecteur);
            if (!deMoi && !m.isLu()) m.setLu(true);   // lu à la lecture
            MessageDTO d = new MessageDTO();
            d.setId(m.getIdMessage());
            d.setContenu(m.getContenu());
            d.setDateEnvoi(m.getDateEnvoi());
            d.setDeMoi(deMoi);
            return d;
        }).toList();
    }

    @Override
    public void envoyer(Long idConversation, Long idExpediteur, String contenu) {
        if (contenu == null || contenu.isBlank()) return;
        Conversation c = em.find(Conversation.class, idConversation);
        if (c == null) return;
        Message m = new Message();
        m.setConversation(c);
        m.setExpediteur(em.getReference(Utilisateur.class, idExpediteur));
        m.setContenu(contenu.trim());
        m.setDateEnvoi(LocalDateTime.now());
        m.setLu(false);
        em.persist(m);
        String apercu = contenu.trim();
        c.setDernierMessage(apercu.length() > 120 ? apercu.substring(0, 120) : apercu);
        c.setDateDernierMessage(LocalDateTime.now());

        // Notifie le destinataire (l'autre partie de la conversation).
        Utilisateur client = c.getClient();
        Utilisateur proprietaire = c.getEspace() != null ? c.getEspace().getProprietaire() : null;
        boolean envoyeurEstClient = client != null && client.getIdUtilisateur().equals(idExpediteur);
        Long destinataire = envoyeurEstClient
                ? (proprietaire != null ? proprietaire.getIdUtilisateur() : null)
                : (client != null ? client.getIdUtilisateur() : null);
        String expediteur = envoyeurEstClient
                ? nomAffichage(client)
                : (c.getEspace() != null ? c.getEspace().getNomCommercial() : "un utilisateur");
        String extrait = apercu.length() > 60 ? apercu.substring(0, 60) + "…" : apercu;
        notificationService.notifier(destinataire,
                "Nouveau message de « " + expediteur + " » : " + extrait,
                "/messages.xhtml", "NORMALE");
    }

    @Override
    public long nbNonLus(Long idUtilisateur) {
        if (idUtilisateur == null) return 0;
        return em.createQuery(
                        "select count(m) from Message m "
                                + "where m.lu = false "
                                + "  and m.expediteur.idUtilisateur <> :id "
                                + "  and (m.conversation.client.idUtilisateur = :id "
                                + "       or m.conversation.espace.proprietaire.idUtilisateur = :id)",
                        Long.class)
                .setParameter("id", idUtilisateur)
                .getSingleResult();
    }

    private static String nomAffichage(Utilisateur u) {
        if (u == null) return "un utilisateur";
        if (u.getPrenom() != null && !u.getPrenom().isBlank()) return u.getPrenom();
        return u.getNom() != null ? u.getNom() : "un utilisateur";
    }
}
