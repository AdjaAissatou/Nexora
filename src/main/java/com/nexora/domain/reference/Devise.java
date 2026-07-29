package com.nexora.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Devise de reference (multi-devise : XOF, EUR, USD...). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "devise")
public class Devise implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devise")
    private Long idDevise;

    @Column(name = "code", nullable = false, unique = true, length = 8)
    private String code;

    @Column(name = "libelle", nullable = false, length = 80)
    private String libelle;

    @Column(name = "symbole", length = 8)
    private String symbole;
}
