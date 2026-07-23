package com.nexora.domain.messaging;

import com.nexora.domain.space.EspaceProfessionnel;
import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fil de discussion entre un {@link Utilisateur} (client) et un
 * {@link EspaceProfessionnel}. Regroupe les {@link Message} et les
 * {@link Appel}. Style "WhatsApp Business".
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conversation")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversation")
    private Long idConversation;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "dernier_message", length = 500)
    private String dernierMessage;

    @Column(name = "date_dernier_message")
    private LocalDateTime dateDernierMessage;

    @Column(name = "archive_client")
    private boolean archiveClient = false;

    @Column(name = "archive_professionnel")
    private boolean archiveProfessionnel = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espace")
    private EspaceProfessionnel espace;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appel> appels = new ArrayList<>();
}
