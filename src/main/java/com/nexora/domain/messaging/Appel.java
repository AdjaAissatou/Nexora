package com.nexora.domain.messaging;

import com.nexora.common.enums.TypeAppel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Trace d'un appel audio (ou video, extension future) initie depuis une
 * {@link Conversation}. La signalisation temps reel (WebRTC) est deleguee a un
 * service dedie ; cette entite conserve l'historique.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "appel")
public class Appel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_appel")
    private Long idAppel;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "statut", length = 30)
    private String statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_appel", length = 10)
    private TypeAppel typeAppel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conversation")
    private Conversation conversation;
}
