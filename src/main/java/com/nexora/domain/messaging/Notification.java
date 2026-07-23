package com.nexora.domain.messaging;

import com.nexora.domain.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Notification adressee a un {@link Utilisateur} (in-app / push mobile). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification",
        indexes = @Index(name = "idx_notif_user_lu", columnList = "id_utilisateur,lu"))
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Long idNotification;

    @Column(name = "contenu", nullable = false, length = 500)
    private String contenu;

    @Column(name = "lu")
    private boolean lu = false;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    @Column(name = "lien", length = 500)
    private String lien;

    @Column(name = "importance", length = 20)
    private String importance;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_notification")
    private TypeNotification type;
}
