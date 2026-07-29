package com.nexora.dto;

import com.nexora.domain.messaging.Notification;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Vue exposee d'une notification utilisateur. */
@Getter
@Setter
public class NotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String contenu;
    private String lien;
    private String importance;
    private boolean lu;
    private LocalDateTime dateCreation;

    public NotificationDTO() {
    }

    public NotificationDTO(Notification n) {
        this.id = n.getIdNotification();
        this.contenu = n.getContenu();
        this.lien = n.getLien();
        this.importance = n.getImportance();
        this.lu = n.isLu();
        this.dateCreation = n.getDateCreation();
    }
}
