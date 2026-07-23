package com.nexora.domain.messaging;

import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Message instantane echange au sein d'une {@link Conversation}. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "message",
        indexes = @Index(name = "idx_message_conversation", columnList = "id_conversation"))
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Long idMessage;

    @Column(name = "contenu", columnDefinition = "text")
    private String contenu;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "lu")
    private boolean lu = false;

    @Column(name = "type_message", length = 30)
    private String typeMessage;

    @Column(name = "piece_jointe", length = 500)
    private String pieceJointe;

    @Column(name = "supprime_expediteur")
    private boolean supprimeExpediteur = false;

    @Column(name = "supprime_destinataire")
    private boolean supprimeDestinataire = false;

    @Column(name = "modifie")
    private boolean modifie = false;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conversation")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_expediteur")
    private Utilisateur expediteur;
}
