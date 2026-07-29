package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Vue d'une conversation, du point de vue de l'utilisateur qui la consulte. */
@Getter
@Setter
public class ConversationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titre;              // nom de l'interlocuteur (espace ou client)
    private String dernierMessage;
    private LocalDateTime dateDernierMessage;

    public String getInitiale() {
        return (titre != null && !titre.isBlank()) ? titre.substring(0, 1).toUpperCase() : "?";
    }
}
