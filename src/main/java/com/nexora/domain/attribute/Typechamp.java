package com.nexora.domain.attribute;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Type de champ d'un {@link Attribut} dynamique. Pilote le rendu du formulaire
 * (PrimeFaces) et la strategie de stockage/filtre : TEXT, NUMBER, BOOLEAN, DATE,
 * EMAIL, PHONE, URL, LIST (choix unique), MULTI_LIST (choix multiple).
 * Configurable en base : aucun type n'est code en dur cote metier.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "type_champ")
public class Typechamp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_champ")
    private Long idTypechamp;

    /** Code technique (TEXT, NUMBER, BOOLEAN, DATE, EMAIL, PHONE, URL, LIST, MULTI_LIST). */
    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "libelle", nullable = false, length = 120)
    private String libelle;

    /** Composant PrimeFaces suggere pour le rendu (inputText, spinner, ...). */
    @Column(name = "composant_ui", length = 60)
    private String composantUi;
}
