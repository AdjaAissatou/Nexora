package com.nexora.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Vue d'un message dans une conversation. */
@Getter
@Setter
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private boolean deMoi;            // envoyé par l'utilisateur courant
}
