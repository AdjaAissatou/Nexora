package com.nexora.domain.messaging;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Type configurable de notification (COMMANDE, MESSAGE, PROMOTION, ...). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "type_notification")
public class TypeNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_notification")
    private Long idTypeNotification;

    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;
}
