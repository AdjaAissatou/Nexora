package com.nexora.service;

import com.nexora.dto.NotificationDTO;

import java.util.List;

/** Cas d'usage des notifications utilisateur (création et consultation). */
public interface NotificationService {

    /** Crée une notification pour un utilisateur. */
    void notifier(Long idUtilisateur, String contenu, String lien, String importance);

    /** Notifications de l'utilisateur, plus récentes d'abord. */
    List<NotificationDTO> pourUtilisateur(Long idUtilisateur);

    /** Nombre de notifications non lues. */
    long nbNonLues(Long idUtilisateur);

    /** Marque toutes les notifications de l'utilisateur comme lues. */
    void marquerToutesLues(Long idUtilisateur);
}
