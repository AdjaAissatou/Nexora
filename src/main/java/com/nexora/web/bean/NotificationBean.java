package com.nexora.web.bean;

import com.nexora.dto.NotificationDTO;
import com.nexora.service.NotificationService;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Notifications de l'utilisateur connecté (cloche + page). */
@Named("notif")
@ViewScoped
public class NotificationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private NotificationService notificationService;
    @Inject private SessionBean session;

    public long getCount() {
        return session.isConnecte() ? notificationService.nbNonLues(session.getIdUtilisateur()) : 0;
    }

    public List<NotificationDTO> getListe() {
        return session.isConnecte() ? notificationService.pourUtilisateur(session.getIdUtilisateur()) : List.of();
    }

    private List<NotificationDTO> liste;

    /** Ouverture de la page : on capture la liste puis on marque tout comme lu. */
    public void ouvrir(ComponentSystemEvent e) {
        if (!session.isConnecte()) return;
        this.liste = notificationService.pourUtilisateur(session.getIdUtilisateur());
        notificationService.marquerToutesLues(session.getIdUtilisateur());
    }

    /** Liste figée pour l'affichage de la page (statut lu/non lu d'avant ouverture). */
    public List<NotificationDTO> getListePage() {
        return liste != null ? liste : getListe();
    }
}
